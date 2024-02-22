package com.hanfei.rpc.transport;

import com.hanfei.rpc.model.RpcRequest;

public interface RpcClient {
    Object sendRequest(RpcRequest rpcRequest);
}
