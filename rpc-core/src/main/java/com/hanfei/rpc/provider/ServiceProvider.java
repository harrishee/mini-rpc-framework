package com.hanfei.rpc.provider;

/**
 * 服务提供者接口，用于保存服务实例对象和提供服务实例的检索
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public interface ServiceProvider {

    /**
     * 将服务实例对象添加到服务提供者
     */
    <T> void addServiceProvider(T service, Class<T> serviceClass);

    /**
     * 根据服务名称获取对应的服务实例对象
     */
    Object getServiceProvider(String serviceName);
}
