package com.hanfei.rpc.transport.netty.client;

import com.hanfei.rpc.entity.RpcRequest;
import com.hanfei.rpc.entity.RpcResponse;
import com.hanfei.rpc.serializer.CommonSerializer;
import com.hanfei.rpc.factory.SingletonFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * Netty 客户端处理器，用于处理服务端返回的响应消息
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class NettyClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

    private static final Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);

    private final UnprocessedRequests unprocessedRequests;

    public NettyClientHandler() {
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
    }

    /**
     * 处理传入的 RpcResponse 消息，当通道读取到 RpcResponse 消息时，会自动调用该方法进行处理
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse msg) {
        try {
            logger.info("客户端接收到消息: {}", msg);
            unprocessedRequests.complete(msg);
        } finally {
            // 释放消息资源
            ReferenceCountUtil.release(msg);
        }
    }

    /**
     * 处理异常情况
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("过程调用时有错误发生: {}", cause.getMessage());
        ctx.close();
    }

    /**
     * 处理心跳包事件
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object event) throws Exception {
        // 若是心跳包事件，则发送心跳包
        if (event instanceof IdleStateEvent) {
            // 获取 IdleStateEvent 中的状态
            IdleState state = ((IdleStateEvent) event).state();
            // 判断状态是否为 WRITER_IDLE，若是，则发送心跳包
            if (state == IdleState.WRITER_IDLE) {
                logger.info("发送心跳包 [{}]", ctx.channel().remoteAddress());

                // 获取连接通道
                Channel channel = ChannelProvider.get((InetSocketAddress) ctx.channel().remoteAddress(),
                        CommonSerializer.getByCode(CommonSerializer.DEFAULT_SERIALIZER));
                // 创建心跳包请求
                RpcRequest rpcRequest = new RpcRequest();
                rpcRequest.setHeartBeat(true);
                // 发送心跳包并在发送失败时关闭通道
                channel.writeAndFlush(rpcRequest).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } else {
            // 若不是心跳包事件，则调用父类方法继续处理
            super.userEventTriggered(ctx, event);
        }
    }
}
