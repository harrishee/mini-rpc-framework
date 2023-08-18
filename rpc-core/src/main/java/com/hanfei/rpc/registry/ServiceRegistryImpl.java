package com.hanfei.rpc.registry;

import com.hanfei.rpc.enumeration.RpcErrorMsgEnum;
import com.hanfei.rpc.exception.RpcRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务注册表的实现，用于注册和获取服务
 * 通过维护一个映射表来保存已注册的服务实体
 * 注册的服务按其接口名称进行索引，可以通过接口名称来获取相应的服务实体
 * 该实现使用了线程安全的 ConcurrentHashMap 来保存服务实体，以及一个记录已注册服务接口名称的集合
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class ServiceRegistryImpl implements ServiceRegistry {

    private static final Logger logger = LoggerFactory.getLogger(ServiceRegistryImpl.class);

    // 保存已注册的服务实体的映射表
    private final Map<String, Object> serviceMap = new ConcurrentHashMap<>();

    // 记录已注册服务接口名称的集合
    private final Set<String> registeredService = ConcurrentHashMap.newKeySet();

    /**
     * 注册一个服务实体
     * Register a service object.
     *
     * @param service 待注册的服务实体
     * @param <T>     服务实体类型
     */
    @Override
    public synchronized <T> void register(T service) {
        String serviceName = service.getClass().getCanonicalName();
        if (registeredService.contains(serviceName)) return;

        // 将服务接口名称加入已注册集合
        registeredService.add(serviceName);

        // 获取服务实体实现的所有接口
        Class<?>[] interfaces = service.getClass().getInterfaces();
        if (interfaces.length == 0) {
            throw new RpcRuntimeException(RpcErrorMsgEnum.SERVICE_NOT_IMPLEMENT_ANY_INTERFACE);
        }

        // 将服务实体注册到映射表中，以接口名称作为索引
        for (Class<?> i : interfaces) {
            serviceMap.put(i.getCanonicalName(), service);
        }
        logger.info("向接口: {} 注册服务: {}", interfaces, serviceName);
    }

    /**
     * 根据服务接口名称获取对应的服务实体
     * Retrieve a service object based on its interface name.
     *
     * @param serviceName 服务接口名称
     * @return 服务实体
     * @throws RpcRuntimeException 如果未找到对应的服务实体，则抛出 RpcRuntimeException
     */
    @Override
    public synchronized Object getService(String serviceName) {
        System.out.println(serviceMap);
        System.out.println(registeredService);
        Object service = serviceMap.get(serviceName);
        if (service == null) {
            throw new RpcRuntimeException(RpcErrorMsgEnum.SERVICE_NOT_FOUND);
        }
        logger.info("获取服务: {} 成功", serviceName);
        return service;
    }
}
