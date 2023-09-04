package com.hanfei.rpc.registry;

import java.net.InetSocketAddress;

/**
 * service discovery interface
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public interface ServiceDiscovery {

    InetSocketAddress getServerByService(String serviceName);
}
