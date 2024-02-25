package com.hanfei.rpc.registry;

import java.net.InetSocketAddress;

public interface ServiceRegistry {
    void registerService(String serviceName, InetSocketAddress inetSocketAddress);
}
