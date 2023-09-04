package com.hanfei.rpc.transport.netty.server;

import com.hanfei.rpc.transport.netty.codec.CommonDecoder;
import com.hanfei.rpc.transport.netty.codec.CommonEncoder;
import com.hanfei.rpc.hook.ShutdownHook;
import com.hanfei.rpc.provider.ServiceProviderImpl;
import com.hanfei.rpc.registry.nacos.NacosServiceRegistry;
import com.hanfei.rpc.serialize.CommonSerializer;
import com.hanfei.rpc.transport.AbstractRpcServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * Netty server implementation for handling RPC communication
 * Responsible for starting the server, managing connections, and handling incoming requests
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
@Slf4j
public class NettyServer extends AbstractRpcServer {

    private CommonSerializer serializer;

    public NettyServer(String host, int port, Integer serializer) {
        this.host = host;
        this.port = port;
        this.serializer = CommonSerializer.getByCode(serializer);

        // TODO check
        serviceRegistry = new NacosServiceRegistry();
        serviceProvider = new ServiceProviderImpl();

        serviceScan(); // scan all @Service and publish them
    }

    @Override
    public void start() {
        // register a shutdown hook to clear all registered services on JVM shutdown
        ShutdownHook.getShutdownHook().addClearAllHook();

        // create event loop groups for handling I/O operations
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            // create and configure the server bootstrap
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    // use NioServerSocketChannel
                    .channel(NioServerSocketChannel.class)
                    // logging handler for server's channel
                    .handler(new LoggingHandler(LogLevel.INFO))
                    // the maximum length of the queue to temporarily hold completed three-way handshake requests.
                    // if the conn establishment is frequent and the server creating new connections slowly,
                    // this parameter can be increased appropriately
                    .option(ChannelOption.SO_BACKLOG, 256)
                    // enable the TCP heartbeat mechanism
                    // maintaining an active state of the connection to prevent it from being accidentally closed
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    // enable the Nagle algorithm
                    // transmitting large data packets as much as possible to reduce network transmission
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            // add handlers to the pipeline
                            pipeline.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS))
                                    // my encoder, decoder, and handler
                                    .addLast(new CommonEncoder(serializer))
                                    .addLast(new CommonDecoder())
                                    .addLast(new NettyServerHandler());
                        }
                    });

            // bind and start the server, wait for the server to close
            ChannelFuture future = serverBootstrap.bind(host, port).sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("Error when starting server: {}", e.getMessage());
        } finally {
            // gracefully shutdown the event loop groups
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
