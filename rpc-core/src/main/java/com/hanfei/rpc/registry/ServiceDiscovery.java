package com.hanfei.rpc.registry;

import java.net.InetSocketAddress;

public interface ServiceDiscovery {
    InetSocketAddress discoverService(String serviceName);
}
