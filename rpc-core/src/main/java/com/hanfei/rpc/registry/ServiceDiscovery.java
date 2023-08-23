package com.hanfei.rpc.registry;

import java.net.InetSocketAddress;

/**
 * 服务发现接口
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public interface ServiceDiscovery {

    /**
     * 从 Nacos 服务注册中心查找特定服务的网络地址
     */
    InetSocketAddress lookupService(String serviceName);
}
