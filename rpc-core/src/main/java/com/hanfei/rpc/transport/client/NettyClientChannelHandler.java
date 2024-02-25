package com.hanfei.rpc.transport.client;

import com.hanfei.rpc.model.RpcRequest;
import com.hanfei.rpc.model.RpcResponse;
import com.hanfei.rpc.util.SingletonFactory;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyClientChannelHandler extends SimpleChannelInboundHandler<RpcResponse> {
    private final PendingRequests pendingRequests;
    
    public NettyClientChannelHandler() {
        this.pendingRequests = SingletonFactory.getInstance(PendingRequests.class);
    }
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse rpcResponse) {
        try {
            log.info("Netty客户端处理器，接收到响应: [{}]", rpcResponse);
            
            // 使用接收到的响应完成对应的CompletableFuture
            pendingRequests.completeResponse(rpcResponse);
        } finally {
            // 释放响应对象引用，防止内存泄露
            ReferenceCountUtil.release(rpcResponse);
        }
    }
    
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object event) throws Exception {
        if (event instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) event).state();
            
            // 如果空闲状态是写空闲（没有写入活动）
            if (state == IdleState.WRITER_IDLE) {
                log.info("Netty客户端处理器，发送心跳包: [{}]", ctx.channel().remoteAddress());
                
                // 发送心跳请求
                RpcRequest rpcRequest = new RpcRequest();
                rpcRequest.setHeartBeat(true);
                ctx.channel().writeAndFlush(rpcRequest).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } else {
            super.userEventTriggered(ctx, event);
        }
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("远程方法调用过程中发生异常: {}", cause.getMessage());
        ctx.close();
    }
}
