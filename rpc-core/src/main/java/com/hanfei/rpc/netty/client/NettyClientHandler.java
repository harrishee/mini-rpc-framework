package com.hanfei.rpc.netty.client;

import com.hanfei.rpc.entity.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Netty 客户端处理器，用于处理服务端返回的 响应消息
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class NettyClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

    private static final Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);

    /**
     * 处理传入的 RpcResponse 消息，当通道读取到 RpcResponse 消息时，会自动调用该方法进行处理
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse msg) {
        try {
            logger.info("客户端接收到消息: {}", msg);

            // 定义 AttributeKey 以获取响应对象
            AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse");

            // 将 响应对象设置到通道属性中，以便其他地方可以获取
            ctx.channel().attr(key).set(msg);
            ctx.channel().close();
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
}
