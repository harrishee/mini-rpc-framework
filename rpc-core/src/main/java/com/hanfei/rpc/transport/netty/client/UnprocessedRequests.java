package com.hanfei.rpc.transport.netty.client;

import com.hanfei.rpc.entity.RpcResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;


public class UnprocessedRequests {

    // concurrentHashMap with request IDs and CompletableFuture
    private static ConcurrentHashMap<String, CompletableFuture<RpcResponse>> unprocessedResponseMap = new ConcurrentHashMap<>();

    /**
     *
     */
    public void completeAssociatedFuture(RpcResponse rpcResponse) {
        CompletableFuture<RpcResponse> associatedFuture = unprocessedResponseMap.remove(rpcResponse.getRequestId());

        // complete the associated future with the RPC response if found
        if (associatedFuture != null) {
            associatedFuture.complete(rpcResponse);
        } else {
            throw new IllegalStateException();
        }
    }

    public void put(String requestId, CompletableFuture<RpcResponse> future) {
        unprocessedResponseMap.put(requestId, future);
    }

    public void remove(String requestId) {
        unprocessedResponseMap.remove(requestId);
    }
}
