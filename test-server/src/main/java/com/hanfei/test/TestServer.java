package com.hanfei.test;

import com.hanfei.rpc.api.CalculateService;
import com.hanfei.rpc.api.HelloService;
import com.hanfei.rpc.registry.ServiceRegistry;
import com.hanfei.rpc.registry.ServiceRegistryImpl;
import com.hanfei.rpc.server.RpcServer;

/**
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class TestServer {

    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        CalculateService calculateService = new CalculateServiceImpl();

        ServiceRegistry serviceRegistry = new ServiceRegistryImpl();
        serviceRegistry.register(helloService);
        serviceRegistry.register(calculateService);

        RpcServer rpcServer = new RpcServer(serviceRegistry);
        rpcServer.start(9000);
    }
}
