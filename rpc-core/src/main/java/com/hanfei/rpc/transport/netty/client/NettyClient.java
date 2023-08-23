package com.hanfei.rpc.transport.netty.client;

import com.hanfei.rpc.entity.RpcRequest;
import com.hanfei.rpc.entity.RpcResponse;
import com.hanfei.rpc.enums.ErrorEnum;
import com.hanfei.rpc.exception.RpcException;
import com.hanfei.rpc.registry.NacosServiceRegistry;
import com.hanfei.rpc.registry.ServiceRegistry;
import com.hanfei.rpc.serializer.CommonSerializer;
import com.hanfei.rpc.transport.RpcClient;
import com.hanfei.rpc.util.RpcMessageChecker;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Netty 实现的 RPC 客户端
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class NettyClient implements RpcClient {

    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);

    // 服务注册中心，用于查找远程服务的地址
    private final ServiceRegistry serviceRegistry;

    // 序列化器
    private CommonSerializer serializer;

    // Netty 的 Bootstrap 实例，用于初始化和配置 Netty 客户端
    private static final Bootstrap bootstrap;

    // 初始化 Bootstrap
    static {
        EventLoopGroup group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true);
    }

    // 构造函数，初始化服务注册中心
    public NettyClient() {
        this.serviceRegistry = new NacosServiceRegistry();
    }

    @Override
    public Object sendRequest(RpcRequest rpcRequest) {
        // 检查是否有序列化器
        if (serializer == null) {
            logger.error("未设置序列化器");
            throw new RpcException(ErrorEnum.SERIALIZER_NOT_FOUND);
        }

        // 用于存储调用结果的引用
        AtomicReference<Object> result = new AtomicReference<>(null);

        try {
            // 查找服务的地址
            InetSocketAddress inetSocketAddress = serviceRegistry.lookupService(rpcRequest.getInterfaceName());

            // 获取通道
            Channel channel = ChannelProvider.get(inetSocketAddress, serializer);
            if (channel.isActive()) {
                // 发送 RPC 请求
                channel.writeAndFlush(rpcRequest).addListener(future1 -> {
                    if (future1.isSuccess()) {
                        logger.info("客户端发送消息: {}", rpcRequest);
                    } else {
                        logger.error("发送消息时发生错误: " + future1.cause());
                    }
                });

                // 等待通道关闭
                channel.closeFuture().sync();

                // 从通道属性中获取响应
                AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse" + rpcRequest.getRequestId());
                RpcResponse rpcResponse = channel.attr(key).get();

                // 检查响应是否匹配请求，匹配则返回响应中的数据部分
                RpcMessageChecker.check(rpcRequest, rpcResponse);
                result.set(rpcResponse.getData());
            } else {
                System.exit(0);
            }
        } catch (InterruptedException e) {
            logger.error("发送消息时有错误发生: ", e);
        }
        return result.get();
    }

    @Override
    public void setSerializer(CommonSerializer serializer) {
        this.serializer = serializer;
    }
}
