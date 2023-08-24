package com.hanfei.rpc.provider;

/**
 * 保存和提供服务实例对象
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public interface ServiceProvider {

    /**
     * 将服务实例对象添加到服务提供者
     */
    <T> void addServiceProvider(T service, String serviceName);

    /**
     * 根据服务名称获取对应的服务实例对象
     */
    Object getServiceProvider(String serviceName);
}
