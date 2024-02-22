package com.hanfei.rpc.transport;

import com.hanfei.rpc.anno.Service;
import com.hanfei.rpc.anno.ServiceScan;
import com.hanfei.rpc.enums.ErrorEnum;
import com.hanfei.rpc.exception.RpcException;
import com.hanfei.rpc.provider.ServiceProvider;
import com.hanfei.rpc.registry.ServiceRegistry;
import com.hanfei.rpc.util.ReflectUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Set;

@Slf4j
public abstract class AbstractRpcServer implements RpcServer {
    protected String host;
    protected int port;
    protected ServiceProvider serviceProvider;
    protected ServiceRegistry serviceRegistry;

    /**
     * Register a service to Nacos and save it in local service provider
     */
    @Override
    public <T> void publishService(String serviceName, T service) {
        serviceProvider.registerService(serviceName, service);
        serviceRegistry.registerServiceToServer(serviceName, new InetSocketAddress(host, port));
    }

    /**
     * scan the @Service and publish them as services
     */
    public void serviceScan() {
        // get the fully qualified name of the main class that initiated the scan: SocketServer/NettyServer
        String mainClassName = ReflectUtil.getStackTrace();

        Class<?> startClass;
        try {
            startClass = Class.forName(mainClassName);
            // Check if the @ServiceScan annotation is present on the main class
            if (!startClass.isAnnotationPresent(ServiceScan.class)) {
                log.error("The @ServiceScan annotation is missing");
                throw new RpcException(ErrorEnum.SERVICE_SCAN_PACKAGE_NOT_FOUND);
            }
        } catch (ClassNotFoundException e) {
            log.error("Error when getStackTrace: {}", e.getMessage());
            throw new RpcException(ErrorEnum.UNKNOWN_ERROR);
        }

        // get the base package to scan from the @ServiceScan annotation
        String basePackage = startClass.getAnnotation(ServiceScan.class).value();

        // if the base package is not specified, use the package of the main class
        if ("".equals(basePackage)) {
            basePackage = mainClassName.substring(0, mainClassName.lastIndexOf("."));
        }

        // get a set of classes in the specified base package
        Set<Class<?>> classSet = ReflectUtil.getClasses(basePackage);
        for (Class<?> clazz : classSet) {
            // if a class has the @Service annotation, get its service name
            if (clazz.isAnnotationPresent(Service.class)) {
                String serviceName = clazz.getAnnotation(Service.class).name();
                Object obj;
                try {
                    // create an instance of the class using its default constructor
                    obj = clazz.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    log.error("Error when creating a new instance: {}", e.getMessage());
                    continue;
                }

                // if the service name is not specified, publish the service using its implemented interface names
                if ("".equals(serviceName)) {
                    Class<?>[] interfaces = clazz.getInterfaces();
                    for (Class<?> oneInterface : interfaces) {
                        publishService(oneInterface.getCanonicalName(), obj);
                    }
                } else {
                    // otherwise, publish the service with the specified service name
                    publishService(serviceName, obj);
                }
            }
        }
    }
}
