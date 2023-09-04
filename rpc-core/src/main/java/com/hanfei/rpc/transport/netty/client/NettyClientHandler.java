package com.hanfei.rpc.transport.netty.client;

import com.hanfei.rpc.entity.RpcRequest;
import com.hanfei.rpc.entity.RpcResponse;
import com.hanfei.rpc.factory.SingletonFactory;
import com.hanfei.rpc.serialize.CommonSerializer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * Netty client handler
 * Responsible for handling RPC responses, sending heartbeats, and managing exceptions
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
@Slf4j
public class NettyClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

    // managing unprocessed requests and their completion
    private final UnprocessedRequests unprocessedRequests;

    // initialize the handler with an instance of UnprocessedRequests using SingletonFactory
    public NettyClientHandler() {
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse rpcResponse) {
        try {
            log.info("The client receives the response: [{}]", rpcResponse);

            // complete the corresponding CompletableFuture of the request with the received response
            unprocessedRequests.completeAssociatedFuture(rpcResponse);
        } finally {
            // release the reference of the response to prevent memory leaks
            ReferenceCountUtil.release(rpcResponse);
        }
    }

    /**
     * Handling heartbeat packets
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object event) throws Exception {
        if (event instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) event).state();

            // if the idle state is WRITER_IDLE (no write activity)
            if (state == IdleState.WRITER_IDLE) {
                log.info("Sending heartbeat to server [{}]", ctx.channel().remoteAddress());

                // retrieve the channel for communication
                Channel channel = ChannelProvider.getChannel(
                        (InetSocketAddress) ctx.channel().remoteAddress(),
                        CommonSerializer.getByCode(CommonSerializer.DEFAULT_SERIALIZER)
                );

                // send a heartbeat packet to the channel
                RpcRequest rpcRequest = new RpcRequest();
                rpcRequest.setHeartBeat(true);
                channel.writeAndFlush(rpcRequest).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } else {
            super.userEventTriggered(ctx, event);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("An exception occurred during the remote method call process: {}", cause.getMessage());
        ctx.close();
    }
}
