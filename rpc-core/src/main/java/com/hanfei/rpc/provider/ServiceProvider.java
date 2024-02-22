package com.hanfei.rpc.provider;

public interface ServiceProvider {
    <T> void registerService(String serviceName, T serviceInstance);

    Object getServiceInstanceByName(String serviceName);
}
