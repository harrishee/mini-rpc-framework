package com.hanfei.rpc.transport.server;

import com.hanfei.rpc.provider.LocalServiceProvider;
import com.hanfei.rpc.registry.NacosServiceRegistry;
import com.hanfei.rpc.serializer.Serializer;
import com.hanfei.rpc.transport.RpcServerBase;
import com.hanfei.rpc.transport.codec.NettyDecoder;
import com.hanfei.rpc.transport.codec.NettyEncoder;
import com.hanfei.rpc.util.JVMUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class NettyServer extends RpcServerBase {
    public NettyServer(String host, int port) {
        this(host, port, Serializer.DEFAULT_SERIALIZER);
    }
    
    public NettyServer(String host, int port, Integer serializerCode) {
        this.host = host;
        this.port = port;
        this.serializer = Serializer.getSerializer(serializerCode);
        this.serviceProvider = new LocalServiceProvider();
        this.serviceRegistry = new NacosServiceRegistry();
        
        serviceScan();
    }
    
    @Override
    public void start() {
        JVMUtil.addShutdownHook();
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 256)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new IdleStateHandler(10, 0, 0, TimeUnit.SECONDS))
                                    .addLast(new NettyEncoder(serializer))
                                    .addLast(new NettyDecoder())
                                    .addLast(new NettyServerChannelHandler()); // 自定义数据处理器
                        }
                    });
            
            // 绑定服务器并启动
            ChannelFuture future = serverBootstrap.bind(host, port).sync();
            log.info("Netty服务器，启动成功，监听地址 [{} : {}]", host, port);
            
            // 等待服务器关闭
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Netty服务器，启动服务器时出错: ", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
