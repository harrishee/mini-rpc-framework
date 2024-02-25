package com.hanfei.rpc.provider;

public interface ServiceProvider {
    <T> void putServiceInstance(String serviceName, T serviceInstance);

    Object getServiceInstance(String serviceName);
}
