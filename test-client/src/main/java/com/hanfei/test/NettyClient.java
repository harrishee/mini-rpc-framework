package com.hanfei.test;

import com.hanfei.rpc.api.CalculateService;
import com.hanfei.rpc.api.HelloPOJO;
import com.hanfei.rpc.api.HelloService;
import com.hanfei.rpc.serialize.CommonSerializer;
import com.hanfei.rpc.transport.RpcClient;
import com.hanfei.rpc.transport.RpcClientProxy;


public class NettyClient {

    public static void main(String[] args) {
        RpcClient client = new com.hanfei.rpc.transport.netty.client.NettyClient(CommonSerializer.KRYO_SERIALIZER);
        RpcClientProxy proxy = new RpcClientProxy(client);

        HelloService helloService = proxy.getProxy(HelloService.class);
        CalculateService calculateService = proxy.getProxy(CalculateService.class);

        HelloPOJO helloPOJO = new HelloPOJO(
                "NettyClient - Harris",
                "NettyServer - Harris",
                "Hello, I am Harris from NettyClient"
        );
        String msg = helloService.sayHello(helloPOJO);
        System.out.println(msg);

        int a = 66, b = 77;
        String addRes = calculateService.addition(a, b, "Message from netty client, sending a=" + a + ", b=" + b);
        System.out.println(addRes);
        String mulRes = calculateService.multiplication(a, b, "Message from netty client, sending a=" + a + ", b=" + b);
        System.out.println(mulRes);
        String divRes = calculateService.division(a, b, "Message from netty client, sending a=" + a + ", b=" + b);
        System.out.println(divRes);
    }
}
