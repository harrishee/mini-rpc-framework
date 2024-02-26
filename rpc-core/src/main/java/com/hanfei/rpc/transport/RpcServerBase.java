package com.hanfei.rpc.transport;

import com.hanfei.rpc.anno.Service;
import com.hanfei.rpc.anno.ServiceScan;
import com.hanfei.rpc.enums.ErrorEnum;
import com.hanfei.rpc.exception.RpcException;
import com.hanfei.rpc.provider.ServiceProvider;
import com.hanfei.rpc.registry.ServiceRegistry;
import com.hanfei.rpc.serializer.Serializer;
import com.hanfei.rpc.util.ReflectUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.Set;

@Slf4j
public abstract class RpcServerBase implements RpcServer {
    protected String host;
    protected int port;
    protected Serializer serializer;
    protected ServiceProvider serviceProvider;
    protected ServiceRegistry serviceRegistry;
    
    @Override
    public <T> void publishService(String serviceName, T serviceInstance) {
        serviceProvider.putServiceInstance(serviceName, serviceInstance);
        serviceRegistry.registerService(serviceName, new InetSocketAddress(host, port));
        log.info("根服务端，服务 [{}] 注册成功", serviceName);
    }
    
    public void serviceScan() {
        // 获取当前运行的主类名
        String currentClassName = ReflectUtil.getCurrentClassName();
        try {
            Class<?> currentClass = Class.forName(currentClassName);
            if (!currentClass.isAnnotationPresent(ServiceScan.class)) {
                throw new RpcException(ErrorEnum.SERVICE_SCAN_PACKAGE_NOT_FOUND);
            }
            
            // 从@ServiceScan注解中获取需要扫描的包名
            String basePackage = currentClass.getAnnotation(ServiceScan.class).value();
            basePackage = basePackage.isEmpty() ? currentClassName.substring(0, currentClassName.lastIndexOf('.')) : basePackage;
            
            Set<Class<?>> classSet = ReflectUtil.getAllClass(basePackage);
            for (Class<?> clazz : classSet) {
                if (clazz.isAnnotationPresent(Service.class)) {
                    String serviceName = clazz.getAnnotation(Service.class).name();
                    Object serviceInstance = clazz.getDeclaredConstructor().newInstance();
                    
                    // 如果没有指定服务名称，则使用类实现的接口名作为服务名
                    if (serviceName.isEmpty()) {
                        for (Class<?> i : clazz.getInterfaces()) {
                            publishService(i.getCanonicalName(), serviceInstance);
                        }
                    } else {
                        publishService(serviceName, serviceInstance);
                    }
                }
            }
            log.info("根服务端，服务扫描完成");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
            throw new RpcException("服务扫描时出现错误", e);
        }
    }
}
