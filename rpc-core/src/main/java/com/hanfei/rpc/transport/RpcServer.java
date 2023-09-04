package com.hanfei.rpc.transport;

import com.hanfei.rpc.serialize.CommonSerializer;

/**
 * RPC server interface
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public interface RpcServer {

    int DEFAULT_SERIALIZER = CommonSerializer.KRYO_SERIALIZER;

    void start();

    <T> void publishService(String serviceName, T service);
}
