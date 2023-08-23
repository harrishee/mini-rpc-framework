package com.hanfei.rpc.transport;

import com.hanfei.rpc.entity.RpcRequest;
import com.hanfei.rpc.entity.RpcResponse;
import com.hanfei.rpc.transport.netty.client.NettyClient;
import com.hanfei.rpc.transport.socket.client.SocketClient;
import com.hanfei.rpc.util.MessageCheckUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * 客户端的动态代理类，用于生成远程服务接口的代理对象
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class RpcClientProxy implements InvocationHandler {

    private static final Logger logger = LoggerFactory.getLogger(RpcClientProxy.class);

    private final RpcClient client;

    public RpcClientProxy(RpcClient client) {
        this.client = client;
    }

    /**
     * 获取服务接口的代理对象
     *
     * @param clazz 服务接口的 Class 对象
     * @param <T>   服务接口类型
     * @return 服务接口的代理对象
     */
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
        // 使用 Proxy.newProxyInstance 创建代理对象
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
    }

    /**
     * 动态代理的方法调用逻辑
     *
     * @param proxy  代理对象
     * @param method 调用的方法
     * @param args   方法参数
     * @return 远程方法调用的结果
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        logger.info("代理对象调用方法: {}#{}", method.getDeclaringClass().getName(), method.getName());

        // 构建RPC请求
        RpcRequest rpcRequest = new RpcRequest(
                UUID.randomUUID().toString(),
                method.getDeclaringClass().getName(),
                method.getName(),
                args,
                method.getParameterTypes(),
                false
        );
        RpcResponse rpcResponse = null;

        // 根据客户端类型，采用不同的方式发送请求并获取响应
        if (client instanceof NettyClient) {
            // 如果是 Netty 客户端，使用 CompletableFuture 异步发送请求，并等待响应
            CompletableFuture<RpcResponse> completableFuture = (CompletableFuture<RpcResponse>) client.sendRequest(rpcRequest);
            try {
                // 获取异步响应结果
                rpcResponse = completableFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                logger.error("方法调用请求发送失败", e);
                return null;
            }
        }

        // 如果是 Socket 客户端，直接发送请求并获取响应
        if (client instanceof SocketClient) {
            rpcResponse = (RpcResponse) client.sendRequest(rpcRequest);
        }

        // 校验请求和响应的匹配性并获取数据部分
        MessageCheckUtil.check(rpcRequest, rpcResponse);
        return rpcResponse.getData();
    }
}
