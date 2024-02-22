package com.hanfei.rpc.provider;

import com.hanfei.rpc.enums.ErrorEnum;
import com.hanfei.rpc.exception.RpcException;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ServiceProviderImpl implements ServiceProvider {
    private static final Map<String, Object> serviceNameToObjectMap = new ConcurrentHashMap<>();

    @Override
    public <T> void registerService(String serviceName, T serviceInstance) {
        if (serviceNameToObjectMap.containsKey(serviceName)) {
            return;
        }
        serviceNameToObjectMap.put(serviceName, serviceInstance);
        log.info("Register service: [{}] to interface: {}", serviceName, serviceInstance.getClass().getInterfaces());
    }

    @Override
    public Object getServiceInstanceByName(String serviceName) {
        Object service = serviceNameToObjectMap.get(serviceName);
        if (service == null) {
            log.error("Can not find service: {}", serviceName);
            throw new RpcException(ErrorEnum.SERVICE_NOT_FOUND);
        }
        log.info("Get service: [{}] from interface: {}", serviceName, service.getClass().getInterfaces());
        return service;
    }
}
