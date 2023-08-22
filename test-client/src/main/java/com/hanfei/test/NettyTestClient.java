package com.hanfei.test;

import com.hanfei.rpc.RpcClient;
import com.hanfei.rpc.RpcClientProxy;
import com.hanfei.rpc.api.CalculateService;
import com.hanfei.rpc.api.HelloObject;
import com.hanfei.rpc.api.HelloService;
import com.hanfei.rpc.netty.client.NettyClient;

/**
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class NettyTestClient {

    public static void main(String[] args) {
        // 创建 NettyClient 实例，连接到指定的服务器地址和端口
        RpcClient client = new NettyClient("127.0.0.1", 9999);
        // 创建 RpcClientProxy 实例，并传入 NettyClient，获得代理对象
        RpcClientProxy proxy = new RpcClientProxy(client);

        // 通过代理获取服务的远程调用接口，并调用方法
        HelloService helloService = proxy.getProxy(HelloService.class);
        HelloObject object = new HelloObject(12, "Message from client, sending 12");
        String res = helloService.hello(object);
        System.out.println(res);

        CalculateService calculateService = proxy.getProxy(CalculateService.class);
        String addRes = calculateService.addNums(66, 77, "Message from client, sending 66 and 77");
        System.out.println(addRes);
    }
}
