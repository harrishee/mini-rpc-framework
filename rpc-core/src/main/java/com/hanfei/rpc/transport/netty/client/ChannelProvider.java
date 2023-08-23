package com.hanfei.rpc.transport.netty.client;

import com.hanfei.rpc.codec.CommonDecoder;
import com.hanfei.rpc.codec.CommonEncoder;
import com.hanfei.rpc.serializer.CommonSerializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 通道提供者，用于获取客户端通道
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class ChannelProvider {

    private static final Logger logger = LoggerFactory.getLogger(ChannelProvider.class);

    private static EventLoopGroup eventLoopGroup;

    private static Bootstrap bootstrap = initializeBootstrap();

    private static Map<String, Channel> channels = new ConcurrentHashMap<>();

    /**
     * 获取客户端通道
     */
    public static Channel get(InetSocketAddress inetSocketAddress, CommonSerializer serializer)
            throws InterruptedException {

        // 构建通道标识，由地址和序列化器编码共同决定
        String key = inetSocketAddress.toString() + serializer.getCode();

        // 判断是否已存在通道
        if (channels.containsKey(key)) {
            Channel channel = channels.get(key);
            // 若通道存在且处于活动状态，直接返回通道
            if (channels != null && channel.isActive()) {
                return channel;
            } else {
                // 否则从映射中移除
                if (channels != null) {
                    channels.remove(key);
                }
            }
        }

        // 初始化通道的处理器
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                // 自定义序列化编解码器
                ch.pipeline()
                        .addLast(new CommonEncoder(serializer))
                        // 添加心跳状态处理器，处理空闲状态
                        .addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS))
                        .addLast(new CommonDecoder())
                        // 添加客户端处理器，处理通道数据的读写
                        .addLast(new NettyClientHandler());
            }
        });
        Channel channel = null;
        try {
            // 连接远程服务器，获取通道
            channel = connect(bootstrap, inetSocketAddress);
        } catch (ExecutionException e) {
            logger.error("连接客户端时有错误发生", e);
            return null;
        }

        // 将通道添加到映射中，返回获取的通道
        channels.put(key, channel);
        return channel;
    }

    /**
     * 连接远程服务器，获取通道
     */
    private static Channel connect(Bootstrap bootstrap, InetSocketAddress inetSocketAddress)
            throws ExecutionException, InterruptedException {

        // 创建一个CompletableFuture实例，用于异步获取连接结果
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();

        // 连接远程服务器，添加连接监听器
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                logger.info("客户端连接成功!");
                // 连接成功时，将通道放入 CompletableFuture 中
                completableFuture.complete(future.channel());
            } else {
                throw new IllegalStateException();
            }
        });

        // 等待连接结果并返回通道
        return completableFuture.get();
    }

    /**
     * 初始化 Bootstrap 实例
     */
    private static Bootstrap initializeBootstrap() {
        // 创建 NIO 事件循环组，用于处理 I/O 事件
        eventLoopGroup = new NioEventLoopGroup();

        // 创建并且配置Bootstrap 实例
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup) // 指定事件循环组
                .channel(NioSocketChannel.class) // 指定通信通道类型为 NIO SocketChannel
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000) // 设置连接的超时时间
                .option(ChannelOption.SO_KEEPALIVE, true) // 是否开启 TCP 底层心跳机制
                .option(ChannelOption.TCP_NODELAY, true); // 是否启用 Nagle 算法
        return bootstrap;
    }
}
