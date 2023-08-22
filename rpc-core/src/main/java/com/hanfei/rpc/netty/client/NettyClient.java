package com.hanfei.rpc.netty.client;

import com.hanfei.rpc.RpcClient;
import com.hanfei.rpc.codec.CommonDecoder;
import com.hanfei.rpc.codec.CommonEncoder;
import com.hanfei.rpc.entity.RpcRequest;
import com.hanfei.rpc.entity.RpcResponse;
import com.hanfei.rpc.serializer.KryoSerializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 客户端的 Netty 通信实现
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class NettyClient implements RpcClient {

    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);

    private String host;

    private int port;

    /**
     * Bootstrap 是 Netty 中的一个启动类，用于初始化和配置 Netty 客户端
     */
    private static final Bootstrap bootstrap;

    /**
     * 构造函数，传入服务器的主机名和端口号
     */
    public NettyClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * 初始化 Netty Bootstrap，在静态初始化块中对 Netty 客户端的初始化和配置进行了设置：
     * 1. 创建一个 NIO EventLoopGroup (group)，用于处理客户端的网络事件
     * 2. 创建 Bootstrap 实例 (bootstrap)，用于配置和初始化客户端
     * 3. 配置 Bootstrap 实例：
     * --- 3.1 指定使用 NioSocketChannel 作为通信通道类型，即 NIO 方式的 Socket 通道
     * --- 3.2 设置 SO_KEEPALIVE 选项为 true，开启 TCP 连接的心跳保活机制
     * 4. 为客户端的 ChannelPipeline 添加处理器
     * --- 4.1 添加数据解码器 CommonDecoder，用于将接收到的数据进行解码
     * --- 4.2 添加数据编码器 CommonEncoder，并使用 JsonSerializer 进行数据编码
     * --- 4.3 添加业务逻辑处理器 NettyClientHandler，用于处理从服务端返回的消息
     */
    static {
        // 创建一个 NIO EventLoopGroup，用于处理客户端的网络事件
        EventLoopGroup group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(group)
                // 指定通信通道类型为 NIO SocketChannel
                .channel(NioSocketChannel.class)
                // 设置 Socket 选项，开启 TCP 连接的心跳保活机制
                .option(ChannelOption.SO_KEEPALIVE, true)
                // 设置初始化客户端 ChannelPipeline 的处理器
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        // 获取客户端的 ChannelPipeline，用于添加处理器
                        ChannelPipeline pipeline = ch.pipeline();
                        // 1 添加数据解码器，用于将接收到的数据进行解码
                        pipeline.addLast(new CommonDecoder())
                                // 2.1 添加数据编码器，并使用 JsonSerializer 进行数据编码
                                // .addLast(new CommonEncoder(new JsonSerializer()))
                                // 2.2 添加数据编码器，并使用 KryoSerializer 进行数据编码
                                .addLast(new CommonEncoder(new KryoSerializer()))
                                // 3 添加业务逻辑处理器 NettyClientHandler
                                .addLast(new NettyClientHandler());
                    }
                });
    }

    @Override
    public Object sendRequest(RpcRequest rpcRequest) {
        try {
            // 创建与服务器的连接
            ChannelFuture future = bootstrap.connect(host, port).sync();
            logger.info("Netty 客户端连接到服务器 {}:{}", host, port);

            // 获取连接的 Channel
            Channel channel = future.channel();
            if (channel != null) {
                // 发送 RpcRequest 并添加监听器
                channel.writeAndFlush(rpcRequest).addListener(future1 -> {
                    if (future1.isSuccess()) {
                        logger.info("客户端发送消息: {}", rpcRequest.toString());
                    } else {
                        logger.error("发送消息时有错误发生: ", future1.cause());
                    }
                });
                // 等待连接关闭
                channel.closeFuture().sync();
                // 定义 AttributeKey 以获取响应对象
                AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse");
                // 从 Channel 的属性中获取响应对象
                RpcResponse rpcResponse = channel.attr(key).get();
                return rpcResponse.getData();
            }

        } catch (InterruptedException e) {
            logger.error("发送消息时有错误发生: ", e);
        }
        return null;
    }
}
