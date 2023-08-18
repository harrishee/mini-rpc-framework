package com.hanfei.test;

import com.hanfei.rpc.api.HelloService;
import com.hanfei.rpc.server.RpcServer;

/**
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class TestServer {

    public static void main(String[] args) {
        // 创建 HelloService 实例
        HelloService helloService = new HelloServiceImpl();

        // 创建 RpcServer 实例
        RpcServer rpcServer = new RpcServer();

        // 注册服务并启动服务器，监听指定端口
        rpcServer.register(helloService, 9000);
    }
}
