package com.hanfei.rpc.transport;

import com.hanfei.rpc.entity.RpcRequest;
import com.hanfei.rpc.serializer.CommonSerializer;

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

    /**
     * 设置序列化器
     */
    void setSerializer(CommonSerializer serializer);
}
