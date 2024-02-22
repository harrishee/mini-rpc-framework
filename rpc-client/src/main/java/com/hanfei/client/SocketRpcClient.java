package com.hanfei.client;

import com.hanfei.api.CalculateService;
import com.hanfei.api.HelloMsg;
import com.hanfei.api.HelloService;
import com.hanfei.rpc.serializer.Serializer;
import com.hanfei.rpc.transport.RpcClientProxy;
import com.hanfei.rpc.transport.socket.client.SocketClient;

public class SocketRpcClient {
    public static void main(String[] args) {
        SocketClient client = new SocketClient(Serializer.KRYO_SERIALIZER);
        RpcClientProxy proxy = new RpcClientProxy(client);

        HelloService helloService = proxy.getProxy(HelloService.class);
        CalculateService calculateService = proxy.getProxy(CalculateService.class);

        HelloMsg helloMsg = new HelloMsg(
                "SocketRpcClient - Harris",
                "SocketServer - Harris",
                "Hello, I am Harris from SocketRpcClient"
        );
        String msg = helloService.sayHello(helloMsg);
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
