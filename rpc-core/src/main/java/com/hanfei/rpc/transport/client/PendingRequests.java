package com.hanfei.rpc.transport.client;

import com.hanfei.rpc.model.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

// 管理和跟踪客户端发起的RPC请求及其对应的响应
@Slf4j
public class PendingRequests {
    // <requestId, CompletableFuture<RpcResponse>>
    private static final ConcurrentHashMap<String, CompletableFuture<RpcResponse<?>>> PENDING_RESPONSE = new ConcurrentHashMap<>();
    
    // 使用RPC响应完成对应的CompletableFuture
    public void completeResponse(RpcResponse<?> rpcResponse) {
        CompletableFuture<RpcResponse<?>> responseFuture = PENDING_RESPONSE.remove(rpcResponse.getRequestId());
        if (responseFuture != null) {
            responseFuture.complete(rpcResponse);
            log.info("Pending表，完成请求ID: [{}] 的CompletableFuture", rpcResponse.getRequestId());
        } else {
            // 如果未找到，抛出异常
            String errMsg = "未找到请求ID: " + rpcResponse.getRequestId() + " 的CompletableFuture";
            log.error("Pending表，" + errMsg);
            throw new IllegalStateException(errMsg);
        }
    }
    
    // 每当客户端发起一个RPC请求时，会创建一个CompletableFuture并通过此方法存储起来
    public void put(String requestId, CompletableFuture<RpcResponse<?>> future) {
        PENDING_RESPONSE.put(requestId, future);
        log.info("Pending表，存储请求ID: [{}] 的CompletableFuture", requestId);
    }
    
    // 移除一个不再需要跟踪的RPC请求的CompletableFuture，一般在请求完成或超时时调用
    public void remove(String requestId) {
        PENDING_RESPONSE.remove(requestId);
        log.info("Pending表，移除请求ID: [{}] 的CompletableFuture", requestId);
    }
}
