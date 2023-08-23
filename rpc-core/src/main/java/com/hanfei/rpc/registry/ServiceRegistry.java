package com.hanfei.rpc.registry;

import java.net.InetSocketAddress;

/**
 * 服务注册中心通用接口，用于服务的注册和查找
 * 目前只有 Nacos 实现类，以后可以试试 Zookeeper
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public interface ServiceRegistry {

    /**
     * 在 Nacos 服务注册中心注册服务实例
     */
    void register(String serviceName, InetSocketAddress inetSocketAddress);

    /**
     * 从 Nacos 服务注册中心查找特定服务的网络地址
     */
    InetSocketAddress lookupService(String serviceName);
}
