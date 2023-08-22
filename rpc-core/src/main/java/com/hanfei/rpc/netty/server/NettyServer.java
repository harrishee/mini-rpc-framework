package com.hanfei.rpc.netty.server;

import com.hanfei.rpc.RpcServer;
import com.hanfei.rpc.codec.CommonDecoder;
import com.hanfei.rpc.codec.CommonEncoder;
import com.hanfei.rpc.serializer.KryoSerializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Netty 服务端
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class NettyServer implements RpcServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    @Override
    public void start(int port) {
        // 创建用于接收连接的 boss 线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        // 创建用于处理连接的 worker 线程组
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            // 创建 ServerBootstrap 实例，用于配置和初始化服务器
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            // 配置 ServerBootstrap 实例
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
                            // 1.1 添加数据编码器 CommonEncoder，并使用 JsonSerializer 进行数据编码
                            // pipeline.addLast(new CommonEncoder(new JsonSerializer()));
                            // 1.2 添加数据编码器 CommonEncoder，并使用 KryoSerializer 进行数据编码
                            pipeline.addLast(new CommonEncoder(new KryoSerializer()));
                            // 2 添加数据解码器 CommonDecoder，用于将接收到的数据进行解码
                            pipeline.addLast(new CommonDecoder());
                            // 3 添加业务逻辑处理器 NettyServerHandler
                            pipeline.addLast(new NettyServerHandler());
                        }
                    });

            // 绑定服务器监听端口，并同步等待绑定完成
            ChannelFuture future = serverBootstrap.bind(port).sync();
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
