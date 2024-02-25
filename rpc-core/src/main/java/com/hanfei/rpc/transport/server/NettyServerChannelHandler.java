package com.hanfei.rpc.transport.server;

import com.hanfei.rpc.model.RpcRequest;
import com.hanfei.rpc.model.RpcResponse;
import com.hanfei.rpc.util.SingletonFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyServerChannelHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private final RpcRequestHandler rpcRequestHandler;
    
    public NettyServerChannelHandler() {
        this.rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
    }
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest rpcRequest) {
        try {
            // 检查是否是心跳包
            if (rpcRequest.isHeartBeat()) {
                log.info("Netty服务器数据处理器，收到心跳包，客户端：[{}]", ctx.channel().remoteAddress());
                return;
            }
            
            log.info("Netty服务器数据处理器，收到请求: [{}]", rpcRequest);
            RpcResponse<?> response = rpcRequestHandler.processRequest(rpcRequest);
            
            // 如果通道处于活动状态且可写，将结果发送回客户端
            if (ctx.channel().isActive() && ctx.channel().isWritable()) {
                ctx.writeAndFlush(response);
            } else {
                log.error("Netty服务器数据处理器，通道不可写，消息发送失败...");
            }
        } finally {
            ReferenceCountUtil.release(rpcRequest);
        }
    }
    
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            // 如果空闲状态是读空闲（未接收到数据），断开连接
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                log.info("Netty服务器数据处理器，长时间未收到心跳包，断开连接：[{}]", ctx.channel().remoteAddress());
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Netty服务器数据处理器，发生异常: ", cause);
        ctx.close();
    }
}
