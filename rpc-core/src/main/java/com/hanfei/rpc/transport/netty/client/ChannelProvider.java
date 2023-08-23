package com.hanfei.rpc.transport.netty.client;

import com.hanfei.rpc.codec.CommonDecoder;
import com.hanfei.rpc.codec.CommonEncoder;
import com.hanfei.rpc.enums.ErrorEnum;
import com.hanfei.rpc.exception.RpcException;
import com.hanfei.rpc.serializer.CommonSerializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
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

    // 初始化 Bootstrap
    private static Bootstrap bootstrap = initializeBootstrap();

    // 最大重试次数
    private static final int MAX_RETRY_COUNT = 5;

    private static Channel channel = null;

    /**
     * 获取客户端通道
     */
    public static Channel get(InetSocketAddress inetSocketAddress, CommonSerializer serializer) {
        // 设置通道初始化器，在连接建立后添加编解码器和处理器
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                // 添加自定义序列化编解码器
                ch.pipeline().addLast(new CommonEncoder(serializer))
                        .addLast(new CommonDecoder())
                        .addLast(new NettyClientHandler());
            }
        });

        // 创建一个倒计数器，等待连接建立完成
        CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            // 连接服务器并等待连接完成
            connect(bootstrap, inetSocketAddress, countDownLatch);
            // 阻塞线程，直到倒计数器归零
            countDownLatch.await();
        } catch (InterruptedException e) {
            logger.error("获取channel时有错误发生: ", e);
        }
        // 返回建立好的通道
        return channel;
    }

    /**
     * 尝试建立连接，使用默认的最大重试次数
     */
    private static void connect(Bootstrap bootstrap, InetSocketAddress inetSocketAddress, CountDownLatch countDownLatch) {
        connect(bootstrap, inetSocketAddress, MAX_RETRY_COUNT, countDownLatch);
    }

    /**
     * 尝试建立连接，支持指定重试次数
     *
     * @param bootstrap         Bootstrap 实例
     * @param inetSocketAddress 服务器地址
     * @param retry             当前重试次数
     * @param countDownLatch    倒计数器，用于等待连接建立完成
     */
    private static void connect(Bootstrap bootstrap, InetSocketAddress inetSocketAddress, int retry, CountDownLatch countDownLatch) {
        // 使用 bootstrap 进行连接，并为连接添加监听器
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                logger.info("客户端连接成功!");
                // 获取建立的通道
                channel = future.channel();
                // 通知倒计数器减少一个
                countDownLatch.countDown();
                return;
            }
            if (retry == 0) {
                logger.error("客户端连接失败:重试次数已用完，放弃连接！");
                // 通知倒计数器减少一个
                countDownLatch.countDown();
                throw new RpcException(ErrorEnum.CLIENT_CONNECT_SERVER_FAILURE);
            }
            // 计算第几次重连
            int order = (MAX_RETRY_COUNT - retry) + 1;
            // 计算本次重连的间隔，采用指数退避算法
            int delay = 1 << order;
            logger.error("{}: 连接失败，第 {} 次重连……", new Date(), order);
            // 使用线程池调度，延时进行下一次连接尝试
            bootstrap.config().group().schedule(() -> connect(bootstrap, inetSocketAddress,
                    retry - 1, countDownLatch), delay, TimeUnit.SECONDS);
        });
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
