package com.hanfei.rpc.provider;

import com.hanfei.rpc.enums.ErrorEnum;
import com.hanfei.rpc.exception.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认的服务提供者实现，用于保存服务端本地的服务实例对象和接口关系
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class ServiceProviderImpl implements ServiceProvider {

    private static final Logger logger = LoggerFactory.getLogger(ServiceProviderImpl.class);

    private static final Map<String, Object> serviceMap = new ConcurrentHashMap<>();

    private static final Set<String> registeredServiceSet = ConcurrentHashMap.newKeySet();

    @Override
    public <T> void addServiceProvider(T service, String serviceName) {
        if (registeredServiceSet.contains(serviceName)) {
            return;
        }
        registeredServiceSet.add(serviceName);
        serviceMap.put(serviceName, service);
        logger.info("向接口: {} 注册服务: {}", service.getClass().getInterfaces(), serviceName);
    }

    @Override
    public Object getServiceProvider(String serviceName) {
        Object service = serviceMap.get(serviceName);
        if (service == null) {
            logger.error("找不到对应的服务: " + serviceName);
            throw new RpcException(ErrorEnum.SERVICE_NOT_FOUND);
        }
        logger.info("获取服务: {} 成功，service: {}", serviceName, service);
        return service;
    }
}
