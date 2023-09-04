package com.hanfei.rpc.transport.netty.client;

import com.hanfei.rpc.serialize.CommonSerializer;
import com.hanfei.rpc.transport.netty.codec.CommonDecoder;
import com.hanfei.rpc.transport.netty.codec.CommonEncoder;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Manages channel creation, reusing active channels, and initializing pipeline handlers
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
@Slf4j
public class ChannelProvider {

    // used for processing I/O events
    private static EventLoopGroup eventLoopGroup;

    // stores the active channels
    private static Map<String, Channel> channelsCache = new ConcurrentHashMap<>();

    // Bootstrap instance for channel initialization
    private static Bootstrap bootstrap = initializeBootstrap();

    private static Bootstrap initializeBootstrap() {
        // initialize the EventLoopGroup for handling I/O events
        eventLoopGroup = new NioEventLoopGroup();

        // initialize the Bootstrap instance with common options
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                // use NioSocketChannel
                .channel(NioSocketChannel.class)
                // set the timeout time for connecting to the server
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true);
        return bootstrap;
    }

    public static Channel getChannel(InetSocketAddress serverAddress, CommonSerializer serializer)
            throws InterruptedException {
        String addressSerializerKey = serverAddress.toString() + serializer.getCode();

        // check if the target is in the cache
        if (channelsCache.containsKey(addressSerializerKey)) {
            Channel channel = channelsCache.get(addressSerializerKey);
            if (channel != null && channel.isActive()) {
                log.info("The client gets the channel from cache: {}", channel);
                return channel;
            } else {
                channelsCache.remove(addressSerializerKey);
            }
        }

        // configure the pipeline for the new channel if not in the cache
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                ch.pipeline()
                        .addLast(new CommonEncoder(serializer))
                        .addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS))
                        .addLast(new CommonDecoder())
                        .addLast(new NettyClientHandler());
            }
        });

        // establish a new channel and add it to the cache
        Channel channel = null;
        try {
            channel = initiateConn(bootstrap, serverAddress);
            log.info("The client gets the channel by new connection: {}", channel);
        } catch (ExecutionException e) {
            log.error("Error during connecting to the server: {}", e.getMessage());
            return null;
        }
        channelsCache.put(addressSerializerKey, channel);
        return channel;
    }

    private static Channel initiateConn(Bootstrap bootstrap, InetSocketAddress serverAddress)
            throws ExecutionException, InterruptedException {

        // create a CompletableFuture to track the result of the connection attempt
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();

        // initiate the connection attempt and attach a listener to handle the result
        bootstrap.connect(serverAddress).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                // complete the CompletableFuture with the connected channel on success
                completableFuture.complete(future.channel());
            } else {
                throw new IllegalStateException();
            }
        });
        // wait for the CompletableFuture to complete and return the connected channel
        return completableFuture.get();
    }
}
