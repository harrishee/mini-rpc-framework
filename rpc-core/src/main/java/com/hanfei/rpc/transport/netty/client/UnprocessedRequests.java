package com.hanfei.rpc.transport.netty.client;

import com.hanfei.rpc.entity.RpcResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 未处理请求容器，用于保存尚未处理完毕的请求结果
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class UnprocessedRequests {

    // 使用 ConcurrentHashMap 存储未处理的请求，保证线程安全
    private static ConcurrentHashMap<String, CompletableFuture<RpcResponse>> unprocessedResponseMap = new ConcurrentHashMap<>();

    // 将请求放入未处理的请求容器中
    public void put(String requestId, CompletableFuture<RpcResponse> future) {
        unprocessedResponseMap.put(requestId, future);
    }

    // 将请求从未处理的请求容器中移除
    public void remove(String requestId) {
        unprocessedResponseMap.remove(requestId);
    }

    /**
     * 完成请求结果，将结果设置到对应的 CompletableFuture 中
     */
    public void complete(RpcResponse rpcResponse) {
        // 从未处理的请求容器中移除对应的请求
        CompletableFuture<RpcResponse> future = unprocessedResponseMap.remove(rpcResponse.getRequestId());
        // 将结果设置到对应的 CompletableFuture 中
        if (future != null) {
            future.complete(rpcResponse);
        } else {
            throw new IllegalStateException();
        }
    }
}
