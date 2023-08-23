package com.hanfei.rpc.transport;

import com.hanfei.rpc.serializer.CommonSerializer;

/**
 * server 通用接口
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public interface RpcServer {

    /**
     * 启动服务
     */
    void start();

    /**
     * 设置序列化器
     */
    void setSerializer(CommonSerializer serializer);

    /**
     * 发布服务
     */
    <T> void publishService(Object service, Class<T> serviceClass);
}
