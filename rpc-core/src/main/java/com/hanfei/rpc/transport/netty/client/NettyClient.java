package com.hanfei.rpc.transport.netty.client;

import com.hanfei.rpc.entity.RpcRequest;
import com.hanfei.rpc.entity.RpcResponse;
import com.hanfei.rpc.enums.ErrorEnum;
import com.hanfei.rpc.exception.RpcException;
import com.hanfei.rpc.factory.SingletonFactory;
import com.hanfei.rpc.loadbalance.LoadBalance;
import com.hanfei.rpc.loadbalance.RoundRobinSelect;
import com.hanfei.rpc.registry.ServiceDiscovery;
import com.hanfei.rpc.registry.nacos.NacosServiceDiscovery;
import com.hanfei.rpc.serialize.CommonSerializer;
import com.hanfei.rpc.transport.RpcClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

/**
 * Netty client implementation for sending RPC requests
 * Responsible for sending RPC requests and managing communication with the server
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
@Slf4j
public class NettyClient implements RpcClient {

    private final ServiceDiscovery serviceDiscovery;

    private final CommonSerializer serializer;

    private static final EventLoopGroup group;

    private static final Bootstrap bootstrap;

    private final UnprocessedRequests unprocessedRequests;

    static {
        // create a new NioEventLoopGroup for handling channel events
        group = new NioEventLoopGroup();
        // create a new Bootstrap instance for configuring and creating client channels
        bootstrap = new Bootstrap();
        // configure the Bootstrap to use the created EventLoopGroup and the NioSocketChannel class
        bootstrap.group(group).channel(NioSocketChannel.class);
    }

    public NettyClient(Integer serializer) {
        LoadBalance loadBalanceStrategy = new RoundRobinSelect();
        // create a NacosServiceDiscovery instance with the specified load balancing strategy
        this.serviceDiscovery = new NacosServiceDiscovery(loadBalanceStrategy);
        // set the serializer by code
        this.serializer = CommonSerializer.getByCode(serializer);
        // obtain an instance of UnprocessedRequests using SingletonFactory
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
    }

    /**
     * Send an RPC request to the server and return a CompletableFuture for the response
     */
    @Override
    public CompletableFuture<RpcResponse> sendRequest(RpcRequest rpcRequest) {
        if (serializer == null) {
            log.error("The serializer is not set");
            throw new RpcException(ErrorEnum.SERIALIZER_NOT_FOUND);
        }

        // create a CompletableFuture for the response
        CompletableFuture<RpcResponse> resultFuture = new CompletableFuture<>();
        try {
            InetSocketAddress serverAddress = serviceDiscovery.getServerByService(rpcRequest.getInterfaceName());
            Channel channel = ChannelProvider.getChannel(serverAddress, serializer);
            log.info("The client has connected to the server, address: [{}]", serverAddress);
            if (channel != null && !channel.isActive()) {
                group.shutdownGracefully();
                return null;
            }

            // put the request into the unprocessedRequests
            unprocessedRequests.put(rpcRequest.getRequestId(), resultFuture);

            // write and flush the RPC request, add a listener to handle response
            channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) future1 -> {
                if (future1.isSuccess()) {
                    log.info("The client has sent the message: [{}]", rpcRequest);
                } else {
                    log.error("An exception occurred while sending a message: ", future1.cause());
                    future1.channel().close();
                    resultFuture.completeExceptionally(future1.cause());
                }
            });
        } catch (InterruptedException e) {
            log.error("Send request exception: {}", e.getMessage());
            unprocessedRequests.remove(rpcRequest.getRequestId());
            Thread.currentThread().interrupt();
        }
        return resultFuture;
    }
}
