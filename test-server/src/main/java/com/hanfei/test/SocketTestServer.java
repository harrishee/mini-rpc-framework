package com.hanfei.test;

import com.hanfei.rpc.api.CalculateService;
import com.hanfei.rpc.api.HelloService;
import com.hanfei.rpc.registry.DefaultServiceRegistry;
import com.hanfei.rpc.registry.ServiceRegistry;
import com.hanfei.rpc.socket.server.SocketServer;

/**
 * Socket 测试服务器，用于启动 Socket 服务器并注册服务
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class SocketTestServer {

    public static void main(String[] args) {
        // 创建服务的实例
        HelloService helloService = new HelloServiceImpl();
        CalculateService calculateService = new CalculateServiceImpl();

        // 创建默认的服务注册表实例，并将服务注册到注册表
        ServiceRegistry serviceRegistry = new DefaultServiceRegistry();
        serviceRegistry.register(helloService);
        serviceRegistry.register(calculateService);

        // 创建 SocketServer 实例，并启动
        SocketServer socketServer = new SocketServer(serviceRegistry);
        socketServer.start(9000);
    }
}
