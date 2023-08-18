package com.hanfei.test;

import com.hanfei.rpc.api.CalculateService;
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

        HelloService helloService = proxy.getProxy(HelloService.class);
        HelloObject object = new HelloObject(12, "Message from client, sending 12");
        String res = helloService.hello(object);
        System.out.println(res);


        CalculateService calculateService = proxy.getProxy(CalculateService.class);
        String addRes = calculateService.addNums(66, 77, "Message from client, sending 66 and 77");
        System.out.println(addRes);
    }
}
