package com.hanfei.rpc.provider;

import com.hanfei.rpc.enums.ErrorEnum;
import com.hanfei.rpc.exception.RpcException;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class LocalServiceProvider implements ServiceProvider {
    private static final Map<String, Object> SERVICE_INSTANCE_MAP = new ConcurrentHashMap<>();
    
    @Override
    public <T> void putServiceInstance(String serviceName, T serviceInstance) {
        if (SERVICE_INSTANCE_MAP.containsKey(serviceName)) {
            log.info("服务注册表，服务 [{}] 已存在，跳过注册", serviceName);
            return;
        }
        
        SERVICE_INSTANCE_MAP.put(serviceName, serviceInstance);
        log.info("服务注册表，注册服务: [{} : {}]", serviceName, serviceInstance.getClass());
    }
    
    @Override
    public Object getServiceInstance(String serviceName) {
        Object service = SERVICE_INSTANCE_MAP.get(serviceName);
        if (service == null) {
            log.error("服务注册表，未找到服务: {}", serviceName);
            throw new RpcException(ErrorEnum.SERVICE_NOT_FOUND);
        }
        
        log.info("服务注册表，获取服务: [{} : {}]", serviceName, service.getClass());
        return service;
    }
}
