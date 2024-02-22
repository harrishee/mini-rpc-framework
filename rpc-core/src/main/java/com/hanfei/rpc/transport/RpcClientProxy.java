package com.hanfei.rpc.transport;

import com.hanfei.rpc.model.RpcRequest;
import com.hanfei.rpc.model.RpcResponse;
import com.hanfei.rpc.transport.netty.client.NettyClient;
import com.hanfei.rpc.transport.socket.client.SocketClient;
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

    public RpcClientProxy(RpcClient client) {
        this.client = client;
    }

    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
        // Create a dynamic proxy instance for the given interface class
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        log.info("Proxy invoke method: {}#{}", method.getDeclaringClass().getName(), method.getName());

        // Create an RPC request object
        RpcRequest rpcRequest = new RpcRequest(
                UUID.randomUUID().toString(),
                method.getDeclaringClass().getName(),
                method.getName(),
                args,
                method.getParameterTypes(),
                false
        );
        RpcResponse rpcResponse = null;

        if (client instanceof NettyClient) {
            try {
                // If using Netty, send the RPC request using CompletableFuture
                CompletableFuture<RpcResponse> completableFuture =
                        (CompletableFuture<RpcResponse>) client.sendRequest(rpcRequest);

                // Wait for the response using the completableFuture.get() method
                rpcResponse = completableFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                log.info("Error when sending request: {}", e.getMessage());
                return null;
            }
        }

        if (client instanceof SocketClient) {
            // Send the RPC request and receive the response synchronously
            rpcResponse = (RpcResponse) client.sendRequest(rpcRequest);
        }

        // Validate the response and get the data part
        MessageUtil.validate(rpcRequest, rpcResponse);
        return rpcResponse.getData();
    }
}
