package com.hanfei.test;

import com.hanfei.rpc.api.CalculateService;
import com.hanfei.rpc.api.HelloObject;
import com.hanfei.rpc.api.HelloService;
import com.hanfei.rpc.serialize.CommonSerializer;
import com.hanfei.rpc.transport.RpcClientProxy;

/**
 * Socket 测试客户端，用于调用远程服务
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
        HelloObject object = new HelloObject(12, "Message from socket client, sending 12");
        String res = helloService.hello(object);
        System.out.println(res);

        CalculateService calculateService = proxy.getProxy(CalculateService.class);
        String addRes = calculateService.addNums(66, 77, "Message from socket client, sending 66 and 77");
        System.out.println(addRes);
    }
}
