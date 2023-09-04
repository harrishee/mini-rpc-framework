package com.hanfei.test;

import com.hanfei.rpc.api.CalculateService;
import com.hanfei.rpc.api.HelloPOJO;
import com.hanfei.rpc.api.HelloService;
import com.hanfei.rpc.serialize.CommonSerializer;
import com.hanfei.rpc.transport.RpcClientProxy;

/**
 * Client side, Socket version
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class SocketClient {

    public static void main(String[] args) {
        com.hanfei.rpc.transport.socket.client.SocketClient client = new com.hanfei.rpc.transport.socket.client.SocketClient(CommonSerializer.KRYO_SERIALIZER);
        RpcClientProxy proxy = new RpcClientProxy(client);

        HelloService helloService = proxy.getProxy(HelloService.class);
        CalculateService calculateService = proxy.getProxy(CalculateService.class);

        HelloPOJO helloPOJO = new HelloPOJO(
                "SocketClient - Harris",
                "SocketServer - Harris",
                "Hello, I am Harris from SocketClient"
        );
        String msg = helloService.sayHello(helloPOJO);
        System.out.println(msg);

        int a = 66, b = 77;
        String addRes = calculateService.addition(a, b, "Message from socket client, sending a=" + a + ", b=" + b);
        System.out.println(addRes);
        String mulRes = calculateService.multiplication(a, b, "Message from socket client, sending a=" + a + ", b=" + b);
        System.out.println(mulRes);
        String divRes = calculateService.division(a, b, "Message from socket client, sending a=" + a + ", b=" + b);
        System.out.println(divRes);
    }
}
