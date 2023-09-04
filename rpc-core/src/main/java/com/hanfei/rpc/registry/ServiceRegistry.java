package com.hanfei.rpc.registry;

import java.net.InetSocketAddress;

/**
 * service registry interface
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public interface ServiceRegistry {

    void registerServiceToServer(String serviceName, InetSocketAddress inetSocketAddress);
}
