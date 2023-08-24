package com.hanfei.rpc.registry;

import java.net.InetSocketAddress;

/**
 * 服务注册接口
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public interface ServiceRegistry {

    /**
     * 在 Nacos 服务注册中心注册服务
     *
     * @param serviceName       服务名称
     * @param inetSocketAddress 提供服务的地址
     */
    void register(String serviceName, InetSocketAddress inetSocketAddress);
}
