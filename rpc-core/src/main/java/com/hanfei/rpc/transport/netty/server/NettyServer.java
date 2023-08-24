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

import java.util.concurrent.TimeUnit;

/**
 * Netty 服务端
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class NettyServer extends AbstractRpcServer {

    private CommonSerializer serializer;

    public NettyServer(String host, int port, Integer serializer) {
        this.host = host;
        this.port = port;
        serviceRegistry = new NacosServiceRegistry();
        serviceProvider = new ServiceProviderImpl();
        this.serializer = CommonSerializer.getByCode(serializer);

        serviceScan();
    }

    @Override
    public void start() {
        ShutdownHook.getShutdownHook().addClearAllHook();
        // 创建用于接收连接的 boss 线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        // 创建用于处理连接的 worker 线程组
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // 指定通信通道类型为 NIO ServerSocketChannel
                    .handler(new LoggingHandler(LogLevel.INFO)) // 添加日志处理器，用于打印日志
                    .option(ChannelOption.SO_BACKLOG, 256) // 设置 TCP 连接的队列大小
                    .option(ChannelOption.SO_KEEPALIVE, true) // 设置 Socket 选项，开启 TCP 连接的心跳保活机制
                    .childOption(ChannelOption.TCP_NODELAY, true) // 设置子 Channel 选项，关闭 Nagle 算法
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS))
                                    .addLast(new CommonEncoder(serializer))
                                    .addLast(new CommonDecoder())
                                    .addLast(new NettyServerHandler());
                        }
                    });

            // 绑定服务器监听地址和端口，并同步等待绑定完成
            ChannelFuture future = serverBootstrap.bind(host, port).sync();
            // 等待服务器的关闭事件
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            logger.error("启动服务器时有错误发生: ", e);
        } finally {
            // 关闭 boss 线程组和 worker 线程组
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
