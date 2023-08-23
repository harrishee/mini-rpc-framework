package com.hanfei.rpc.transport.netty.client;

import com.hanfei.rpc.entity.RpcRequest;
import com.hanfei.rpc.entity.RpcResponse;
import com.hanfei.rpc.enums.ErrorEnum;
import com.hanfei.rpc.exception.RpcException;
import com.hanfei.rpc.loadbalancer.LoadBalancer;
import com.hanfei.rpc.loadbalancer.RandomLoadBalancer;
import com.hanfei.rpc.registry.NacosServiceDiscovery;
import com.hanfei.rpc.registry.ServiceDiscovery;
import com.hanfei.rpc.serializer.CommonSerializer;
import com.hanfei.rpc.transport.RpcClient;
import com.hanfei.rpc.factory.SingletonFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

/**
 * Netty 实现的 RPC 客户端
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class NettyClient implements RpcClient {

    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);

    private final ServiceDiscovery serviceDiscovery;

    private CommonSerializer serializer;

    private static final EventLoopGroup group;

    private static final Bootstrap bootstrap;

    static {
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(group).channel(NioSocketChannel.class);
    }

    private final UnprocessedRequests unprocessedRequests;

    public NettyClient() {
        this(DEFAULT_SERIALIZER, new RandomLoadBalancer());
    }

    public NettyClient(LoadBalancer loadBalancer) {
        this(DEFAULT_SERIALIZER, loadBalancer);
    }

    public NettyClient(Integer serializer) {
        this(serializer, new RandomLoadBalancer());
    }

    public NettyClient(Integer serializer, LoadBalancer loadBalancer) {
        this.serviceDiscovery = new NacosServiceDiscovery(loadBalancer);
        this.serializer = CommonSerializer.getByCode(serializer);
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
    }

    /**
     * 发送 RPC 请求，并异步获取响应结果
     */
    @Override
    public CompletableFuture<RpcResponse> sendRequest(RpcRequest rpcRequest) {
        // 检查序列化器是否设置
        if (serializer == null) {
            logger.error("未设置序列化器");
            throw new RpcException(ErrorEnum.SERIALIZER_NOT_FOUND);
        }

        // 创建用于存储响应结果的 CompletableFuture
        CompletableFuture<RpcResponse> resultFuture = new CompletableFuture<>();
        try {
            // 查找服务的地址
            InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest.getInterfaceName());
            // 获取连接通道
            Channel channel = ChannelProvider.get(inetSocketAddress, serializer);
            // 若通道不可用，则关闭线程组并返回 null
            if (!channel.isActive()) {
                group.shutdownGracefully();
                return null;
            }

            // 将请求添加到未处理请求容器中
            unprocessedRequests.put(rpcRequest.getRequestId(), resultFuture);
            // 发送请求并添加监听器
            channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) future1 -> {
                if (future1.isSuccess()) {
                    logger.info("客户端发送消息: {}", rpcRequest);
                } else {
                    // 若发送失败，则关闭通道并向 resultFuture 中填充异常
                    future1.channel().close();
                    resultFuture.completeExceptionally(future1.cause());
                    logger.error("发送消息时有错误发生: ", future1.cause());
                }
            });
        } catch (InterruptedException e) {
            // 发生中断异常时，从未处理请求容器中移除请求
            unprocessedRequests.remove(rpcRequest.getRequestId());
            logger.error(e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
        return resultFuture;
    }
}
