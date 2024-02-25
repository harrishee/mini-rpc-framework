package com.hanfei.rpc.transport;

import com.hanfei.rpc.model.RpcRequest;
import com.hanfei.rpc.model.RpcResponse;
import com.hanfei.rpc.transport.client.NettyClient;
import com.hanfei.rpc.transport.client.SocketClient;
import com.hanfei.rpc.util.MessageUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
public class RpcClientProxy implements InvocationHandler {
    private final RpcClient client;
    
    public RpcClientProxy(RpcClient rpcClient) {
        this.client = rpcClient;
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        log.info("代理类，调用方法: {},{}", method.getDeclaringClass().getName(), method.getName());
        
        RpcRequest request = new RpcRequest(
                UUID.randomUUID().toString(),
                method.getDeclaringClass().getName(),
                method.getName(),
                args,
                method.getParameterTypes(),
                false
        );
        
        RpcResponse<?> response = null;
        
        // 如果是Netty，使用CompletableFuture异步发送请求
        if (client instanceof NettyClient) {
            try {
                // 启动了异步操作，即向服务器发送RPC请求。这个操作是非阻塞的，即它立即返回一个CompletableFuture对象，而不是等待响应
                CompletableFuture<RpcResponse> future = (CompletableFuture<RpcResponse>) client.sendRequest(request);
                
                // 演示：在等待RPC响应的同时执行其他任务
                new Thread(() -> {
                    for (int i = 0; i < 5; i++) {
                        log.info("模拟执行其他任务 {}", i);
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }).start();
                
                // ** 这里是异步非阻塞的 **
                // 客户端发送请求后会立即返回一个CompletableFuture对象，然后继续执行后续的代码。在这个等待期间，客户端可以做其他操作
                
                // 等待异步操作的完成，并获取结果。这个是阻塞的，会等待直到异步操作完成（服务器响应到达），然后才继续执行后续
                response = future.get();
            } catch (InterruptedException | ExecutionException e) {
                log.info("Error when sending request: {}", e.getMessage());
                return null;
            }
        }
        
        // 如果是Socket，同步发送请求并接收响应
        if (client instanceof SocketClient) {
            
            // ** 这里是同步阻塞的 **
            // 客户端发送请求后会在这里停下来等待，直到收到服务器的响应。在这个等待期间，客户端无法做任何其他操作
            
            response = (RpcResponse<?>) client.sendRequest(request);
        }
        
        MessageUtil.validate(request, response);
        return response.getData();
    }
}
