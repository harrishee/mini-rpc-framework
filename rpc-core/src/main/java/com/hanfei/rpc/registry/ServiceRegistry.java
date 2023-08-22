package com.hanfei.rpc.registry;

/**
 * 服务注册表通用接口
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public interface ServiceRegistry {

    /**
     * 将一个服务注册进注册表
     */
    <T> void register(T service);

    /**
     * 根据服务名称获取服务实体
     */
    Object getService(String serviceName);
}
