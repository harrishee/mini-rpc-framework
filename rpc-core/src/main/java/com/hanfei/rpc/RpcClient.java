package com.hanfei.rpc;

import com.hanfei.rpc.entity.RpcRequest;

/**
 * client 通用接口
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public interface RpcClient {

    /**
     * 发送请求
     */
    Object sendRequest(RpcRequest rpcRequest);
}
