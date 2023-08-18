package com.hanfei.rpc.client;

import com.hanfei.rpc.entity.RpcRequest;
import com.hanfei.rpc.entity.RpcResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 表示客户端的动态代理类，用于生成远程服务接口的代理对象
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class RpcClientProxy implements InvocationHandler {

    private String host;

    private int port;

    public RpcClientProxy(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * 获取服务接口的代理对象
     * @param clazz 服务接口的 Class 对象
     * @return 服务接口的代理对象
     * @param <T> 服务接口类型
     */
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcRequest rpcRequest = RpcRequest.builder()
                .interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameters(args)
                .paramTypes(method.getParameterTypes())
                .build();
        RpcClient rpcClient = new RpcClient();
        return ((RpcResponse<?>) rpcClient.sendRequest(rpcRequest, host, port)).getData();
    }
}
