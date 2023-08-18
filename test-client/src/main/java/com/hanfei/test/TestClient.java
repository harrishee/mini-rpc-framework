package com.hanfei.test;

import com.hanfei.rpc.api.HelloObject;
import com.hanfei.rpc.api.HelloService;
import com.hanfei.rpc.client.RpcClientProxy;

/**
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class TestClient {

    public static void main(String[] args) {
        // 创建 RpcClientProxy，指定服务器地址和端口
        RpcClientProxy proxy = new RpcClientProxy("127.0.0.1", 9000);

        // 获取 HelloService 接口的代理对象
        HelloService helloService = proxy.getProxy(HelloService.class);

        // 创建 HelloObject 对象
        HelloObject object = new HelloObject(12, "Hello from client, my id is 12");

        // 远程调用 hello() 方法
        String res = helloService.hello(object);
        System.out.println(res);

        // 远程调用 addNums() 方法
        String res2 = helloService.addNums(66, 77);
        System.out.println(res2);
    }
}
