package com.hanfei.rpc.transport;

import com.hanfei.rpc.entity.RpcRequest;
import com.hanfei.rpc.serialize.CommonSerializer;


public interface RpcClient {

    int DEFAULT_SERIALIZER = CommonSerializer.KRYO_SERIALIZER;

    Object sendRequest(RpcRequest rpcRequest);
}
