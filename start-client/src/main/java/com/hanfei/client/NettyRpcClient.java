package com.hanfei.client;

import com.hanfei.api.GreetingFileService;
import com.hanfei.api.HelloMsg;
import com.hanfei.api.HelloService;
import com.hanfei.rpc.enums.SerializerEnum;
import com.hanfei.rpc.transport.RpcClient;
import com.hanfei.rpc.transport.RpcClientProxy;
import com.hanfei.rpc.transport.client.NettyClient;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class NettyRpcClient {
    public static void main(String[] args) throws UnknownHostException {
        RpcClient client = new NettyClient(SerializerEnum.KRYO.getCode());
        RpcClientProxy proxy = new RpcClientProxy(client);
        
        HelloService helloService = proxy.getProxy(HelloService.class);
        HelloMsg helloMsg = new HelloMsg(
                InetAddress.getLocalHost().getHostAddress(),
                "This is the 1st from NettyRpcClient!!"
        );
        String msg = helloService.sayHello(helloMsg);
        System.out.println(msg);
        
        HelloMsg helloMsg2 = new HelloMsg(
                InetAddress.getLocalHost().getHostAddress(),
                "This is the 2nd msg from NettyRpcClient!!"
        );
        String msg2 = helloService.sayHello(helloMsg2);
        System.out.println(msg2);
        
        GreetingFileService greetingFileService = proxy.getProxy(GreetingFileService.class);
        String fileName = "greeting-netty.txt";
        String name = "NettyRpcClient-Hanfei";
        String address = InetAddress.getLocalHost().getHostAddress();
        String result = greetingFileService.createGreetingFileToServer(name, address, fileName);
        System.out.println(result);
    }
}
