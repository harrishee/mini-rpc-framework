package com.hanfei.test;

import com.hanfei.rpc.api.CalculateService;
import com.hanfei.rpc.serializer.CommonSerializer;
import com.hanfei.rpc.transport.socket.server.SocketServer;
import com.hanfei.test.ServiceImpl.CalculateServiceImpl;

/**
 * Socket 测试服务器，用于启动 Socket 服务器并注册服务
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class SocketTestServer {

    public static void main(String[] args) {
        // HelloService helloService = new HelloServiceImpl();
        CalculateService calculateService = new CalculateServiceImpl();

        SocketServer socketServer = new SocketServer("127.0.0.1", 9998, CommonSerializer.KRYO_SERIALIZER);
        // socketServer.publishService(helloService, HelloService.class);
        socketServer.publishService(calculateService, CalculateService.class);
    }
}
