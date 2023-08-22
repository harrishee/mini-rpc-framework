package com.hanfei.rpc.netty.server;

import com.hanfei.rpc.RequestHandler;
import com.hanfei.rpc.entity.RpcRequest;
import com.hanfei.rpc.entity.RpcResponse;
import com.hanfei.rpc.registry.ServiceRegistry;
import com.hanfei.rpc.registry.DefaultServiceRegistry;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Netty 服务器处理器，负责处理客户端发送的 请求对象
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private static final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);

    /**
     * 处理请求的 请求处理器
     */
    private static RequestHandler requestHandler;

    /**
     * 服务注册表，用于获取服务实例
     */
    private static ServiceRegistry serviceRegistry;

    /**
     * 静态初始化块，创建 请求处理器 和 服务注册表 实例
     */
    static {
        requestHandler = new RequestHandler();
        serviceRegistry = new DefaultServiceRegistry();
    }

    /**
     * 处理客户端发送的 请求对象
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) {
        try {
            logger.info("1. 服务器接收到请求: {}", msg);
            // 获取接口名称，用于获取服务实例
            String interfaceName = msg.getInterfaceName();
            logger.info("2. 服务器获得接口名: {}", interfaceName);

            // 根据接口名称从服务注册表获取对应的服务实体
            Object service = serviceRegistry.getService(interfaceName);
            logger.info("3. 服务器获得服务实体: {}", service);

            // 调用 请求处理器 处理客户端请求，得到执行结果
            Object result = requestHandler.handle(msg, service);
            logger.info("4. 服务器获得执行结果: {}", result);

            // 将执行结果封装成响应对象并通过 ChannelHandlerContext 发送给客户端
            ChannelFuture future = ctx.writeAndFlush(RpcResponse.success(result));
            logger.info("5. 服务器将执行结果封装成响应对象并通过 ChannelHandlerContext 发送给客户端...");

            // 添加监听器，在发送完成后关闭连接
            future.addListener(ChannelFutureListener.CLOSE);
        } finally {
            // 释放资源
            ReferenceCountUtil.release(msg);
            logger.info("6. 服务器释放资源...");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("处理过程调用时有错误发生: {}", cause.getMessage());
        ctx.close();
    }
}
