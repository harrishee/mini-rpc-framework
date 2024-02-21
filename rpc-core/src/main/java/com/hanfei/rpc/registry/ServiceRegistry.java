package com.hanfei.rpc.registry;

import java.net.InetSocketAddress;


public interface ServiceRegistry {

    void registerServiceToServer(String serviceName, InetSocketAddress inetSocketAddress);
}
