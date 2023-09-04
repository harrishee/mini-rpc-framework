package com.hanfei.rpc.provider;

/**
 * service provider interface
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public interface ServiceProvider {

    <T> void registerService(String serviceName, T serviceInstance);

    Object getServiceInstanceByName(String serviceName);
}
