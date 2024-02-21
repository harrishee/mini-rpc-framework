package com.hanfei.rpc.transport.netty.server;

import com.hanfei.rpc.entity.RpcRequest;
import com.hanfei.rpc.entity.RpcResponse;
import com.hanfei.rpc.factory.SingletonFactory;
import com.hanfei.rpc.transport.handler.RpcRequestHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private final RpcRequestHandler rpcRequestHandler;

    public NettyServerHandler() {
        this.rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
    }

    /**
     * processing the received data and returning the result
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest rpcRequest) {
        try {
            log.info("The server receives the request: [{}]", rpcRequest);

            // check if it is a heartbeat package
            if (rpcRequest.getHeartBeat()) {
                log.info("Receiving heartbeat from client: [{}]", ctx.channel().remoteAddress());
                return;
            }

            // process the request and get the result
            Object result = rpcRequestHandler.handleRequest(rpcRequest);

            // send the result back to the client
            if (ctx.channel().isActive() && ctx.channel().isWritable()) {
                ctx.writeAndFlush(RpcResponse.success(rpcRequest.getRequestId(), result));
            } else {
                log.error("The channel is not writable");
            }
        } finally {
            ReferenceCountUtil.release(rpcRequest);
        }
    }

    /**
     * handling non-I/O events like idle states
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            // get the specific idle state from the event
            IdleState state = ((IdleStateEvent) evt).state();

            // if the idle state is READ_IDLE (no data received)
            if (state == IdleState.READER_IDLE) {
                log.info("The connection is disconnected due to heartbeat timeout...");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    /**
     * handling exceptions that occur during I/O operations in the channel
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("An exception occurred during the remote method call process: {}", cause.getMessage());
        ctx.close();
    }
}
