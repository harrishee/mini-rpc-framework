package com.hanfei.rpc.transport.client;

import com.hanfei.rpc.loadbalancer.LoadBalancer;
import com.hanfei.rpc.loadbalancer.RoundRobin;
import com.hanfei.rpc.model.RpcRequest;
import com.hanfei.rpc.model.RpcResponse;
import com.hanfei.rpc.registry.NacosServiceDiscovery;
import com.hanfei.rpc.registry.ServiceDiscovery;
import com.hanfei.rpc.serializer.Serializer;
import com.hanfei.rpc.transport.RpcClient;
import com.hanfei.rpc.transport.codec.NettyDecoder;
import com.hanfei.rpc.transport.codec.NettyEncoder;
import com.hanfei.rpc.util.SingletonFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NettyClient implements RpcClient {
    private final Serializer serializer;
    private final ServiceDiscovery serviceDiscovery;
    private final PendingRequests pendingRequests; // 用于存储和跟踪客户端发出的RPC请求及其未来的响应
    private static final EventLoopGroup GROUP = new NioEventLoopGroup(); // 用于处理Netty客户端的所有I/O操作和事件
    private static final Bootstrap BOOTSTRAP = new Bootstrap(); // 用于配置客户端启动参数，初始化客户端连接设置
    private static final Map<String, Channel> CHANNEL_CACHE = new ConcurrentHashMap<>();
    
    static {
        BOOTSTRAP.group(GROUP)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true);
    }
    
    public NettyClient(Integer serializerCode) {
        this(serializerCode, new RoundRobin());
    }
    
    public NettyClient(Integer serializerCode, LoadBalancer loadBalancer) {
        this.serializer = Serializer.getSerializer(serializerCode);
        this.serviceDiscovery = new NacosServiceDiscovery(loadBalancer);
        this.pendingRequests = SingletonFactory.getInstance(PendingRequests.class);
        BOOTSTRAP.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast(new NettyEncoder(serializer))
                        .addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS))
                        .addLast(new NettyDecoder())
                        .addLast(new NettyClientChannelHandler()); // 自定义客户端处理器
            }
        });
    }
    
    @Override
    public CompletableFuture<RpcResponse<?>> sendRequest(RpcRequest rpcRequest) {
        CompletableFuture<RpcResponse<?>> responseFuture = new CompletableFuture<>();
        try {
            InetSocketAddress serverAddress = serviceDiscovery.discoverService(rpcRequest.getInterfaceName());
            log.info("Netty客户端，1. 获取服务器地址: [{}]", serverAddress);
            
            Channel channel = getChannel(serverAddress);
            if (channel == null || !channel.isActive()) {
                log.error("Netty客户端，3. 获取通道失败");
                throw new IllegalStateException("通道未激活");
            }
            log.info("Netty客户端，3. 获取通道成功: [{}]", channel);
            
            // 一个requestId对应一个CompletableFuture，用于存储和管理客户端未处理的RPC请求的响应
            pendingRequests.put(rpcRequest.getRequestId(), responseFuture);
            
            // 写入并刷新RPC请求，添加监听器以处理响应
            channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) channelFuture -> {
                if (channelFuture.isSuccess()) {
                    log.info("Netty客户端，4. 发送请求: [{}]", rpcRequest);
                } else {
                    log.error("Netty客户端，4. 发送请求失败: ", channelFuture.cause());
                    
                    // 发送失败，关闭通道，完成异常的CompletableFuture
                    channelFuture.channel().close();
                    responseFuture.completeExceptionally(channelFuture.cause());
                }
            });
        } catch (Exception e) {
            // 中断异常，移除未处理的请求
            log.error("Netty客户端，发送请求时发生错误: ", e);
            pendingRequests.remove(rpcRequest.getRequestId());
            responseFuture.completeExceptionally(e);
        }
        
        return responseFuture;
    }
    
    private Channel getChannel(InetSocketAddress serverAddress) {
        if (serverAddress == null) {
            log.error("Netty客户端，2. 未发现可用服务器地址");
            throw new IllegalStateException("未发现可用服务器地址");
        }
        
        String channelKey = serverAddress.toString() + "#" + serializer.getCode();
        log.info("Netty客户端，2. 尝试获取通道: [{}]", channelKey);
        
        return CHANNEL_CACHE.computeIfAbsent(channelKey, key -> {
            ChannelFuture connectFuture = BOOTSTRAP.connect(serverAddress);
            try {
                // 阻塞等待连接成功
                return connectFuture.sync().channel();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Netty客户端，连接服务器时发生错误", e);
            }
        });
    }
}
