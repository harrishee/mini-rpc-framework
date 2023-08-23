package com.hanfei.test;

import com.hanfei.rpc.api.CalculateService;
import com.hanfei.rpc.serializer.KryoSerializer;
import com.hanfei.rpc.transport.RpcClient;
import com.hanfei.rpc.transport.RpcClientProxy;
import com.hanfei.rpc.transport.netty.client.NettyClient;

/**
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class NettyTestClient {

    public static void main(String[] args) {
        RpcClient client = new NettyClient();
        client.setSerializer(new KryoSerializer());
        RpcClientProxy proxy = new RpcClientProxy(client);

        // 通过代理获取服务的远程调用接口，并调用方法
        // HelloService helloService = proxy.getProxy(HelloService.class);
        // HelloObject object = new HelloObject(12, "Message from netty client, sending 12");
        // String res = helloService.hello(object);
        // System.out.println(res);

        CalculateService calculateService = proxy.getProxy(CalculateService.class);
        String addRes = calculateService.addNums(66, 77, "Message from netty client, sending 66 and 77");
        System.out.println(addRes);
    }
}
