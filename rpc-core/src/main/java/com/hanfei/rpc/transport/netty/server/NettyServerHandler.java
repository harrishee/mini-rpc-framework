package com.hanfei.rpc.transport.netty.server;

import com.hanfei.rpc.entity.RpcRequest;
import com.hanfei.rpc.entity.RpcResponse;
import com.hanfei.rpc.handler.RequestHandler;
import com.hanfei.rpc.util.ThreadPoolFactory;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

/**
 * Netty 服务器处理器，负责处理客户端发送的 请求对象
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private static final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);

    // 处理请求的 请求处理器
    private static RequestHandler requestHandler;

    private static final String THREAD_NAME_PREFIX = "netty-server-handler";

    private static final ExecutorService threadPool;

    // 静态初始化块，创建线程池和请求处理器
    static {
        requestHandler = new RequestHandler();
        threadPool = ThreadPoolFactory.createDefaultThreadPool(THREAD_NAME_PREFIX);
    }

    /**
     * 处理客户端发送的 请求对象
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) {
        threadPool.execute(() -> {
            try {
                logger.info("1. 服务器接收到请求: {}", msg);

                // 调用 请求处理器 处理客户端请求，得到执行结果
                Object result = requestHandler.handle(msg);
                logger.info("2. 服务器获得执行结果: {}", result);

                // 将执行结果封装成响应对象并通过 ChannelHandlerContext 发送给客户端
                ChannelFuture future = ctx.writeAndFlush(RpcResponse.success(result, msg.getRequestId()));
                logger.info("3. 服务器将执行结果封装成响应对象并通过 ChannelHandlerContext 发送给客户端...");

                // 添加监听器，在发送完成后关闭连接
                future.addListener(ChannelFutureListener.CLOSE);
            } finally {
                // 释放资源
                ReferenceCountUtil.release(msg);
                logger.info("4. 服务器释放资源...");
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("处理过程调用时有错误发生: {}", cause.getMessage());
        ctx.close();
    }
}
