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

    // 保存已注册的服务对象的映射表，用 static 确保全局唯一
    private static final Map<String, Object> serviceMap = new ConcurrentHashMap<>();

    // 记录已注册服务接口名称的集合，用 static 确保全局唯一
    private static final Set<String> registeredServiceSet = ConcurrentHashMap.newKeySet();

    /**
     * 在注册表中注册服务对象
     */
    @Override
    public synchronized <T> void addServiceProvider(T service) {
        String serviceName = service.getClass().getCanonicalName();
        if (registeredServiceSet.contains(serviceName)) return;

        // 将服务接口名称添加到已注册集合中
        registeredServiceSet.add(serviceName);

        // 获取服务对象实现的所有接口
        Class<?>[] interfaces = service.getClass().getInterfaces();
        if (interfaces.length == 0) {
            throw new RpcException(ErrorEnum.SERVICE_NOT_IMPLEMENT_ANY_INTERFACE);
        }

        // 使用接口名称作为键将服务对象注册到映射表中
        for (Class<?> i : interfaces) {
            serviceMap.put(i.getCanonicalName(), service);
        }
        logger.info("向接口: {} 注册服务: {}，目前 serviceMap: {}, registeredServiceSet: {}",
                interfaces, serviceName, serviceMap, registeredServiceSet);
    }

    /**
     * 根据接口名称检索服务对象
     */
    @Override
    public synchronized Object getServiceProvider(String serviceName) {
        Object service = serviceMap.get(serviceName);
        if (service == null) {
            throw new RpcException(ErrorEnum.SERVICE_NOT_FOUND);
        }
        logger.info("获取服务: {} 成功，响应: {}", serviceName, service);
        return service;
    }
}
