package com.hanfei.test;

import com.hanfei.rpc.api.CalculateService;
import com.hanfei.rpc.api.HelloService;
import com.hanfei.rpc.netty.server.NettyServer;
import com.hanfei.rpc.registry.DefaultServiceRegistry;
import com.hanfei.rpc.registry.ServiceRegistry;

/**
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class NettyTestServer {

    public static void main(String[] args) {
        // 创建服务的实例
        HelloService helloService = new HelloServiceImpl();
        CalculateService calculateService = new CalculateServiceImpl();

        // 创建默认的服务注册表实例，并将服务注册到注册表
        ServiceRegistry registry = new DefaultServiceRegistry();
        registry.register(calculateService);
        registry.register(helloService);

        // 创建 NettyServer 实例，并启动
        NettyServer server = new NettyServer();
        server.start(9999);
    }
}
