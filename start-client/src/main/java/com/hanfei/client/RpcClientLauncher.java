package com.hanfei.client;

import com.hanfei.api.GreetingFileService;
import com.hanfei.api.HelloMsg;
import com.hanfei.api.HelloService;
import com.hanfei.rpc.enums.SerializerEnum;
import com.hanfei.rpc.transport.RpcClient;
import com.hanfei.rpc.transport.RpcClientProxy;
import com.hanfei.rpc.transport.client.NettyClient;
import com.hanfei.rpc.transport.client.SocketClient;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class RpcClientLauncher {
    public static void main(String[] args) throws UnknownHostException {
        RpcClient client;
        String clientType = System.getProperty("clientType", "netty");
        switch (clientType) {
            case "netty":
                client = new NettyClient(SerializerEnum.KRYO.getCode());
                break;
            case "socket":
                client = new SocketClient(SerializerEnum.KRYO.getCode());
                break;
            default:
                throw new IllegalArgumentException("未知的客户端类型: " + clientType);
        }
        RpcClientProxy proxy = new RpcClientProxy(client);
        
        // ********** 调用远程服务的示例 **********
        
        HelloService helloService = proxy.getProxy(HelloService.class);
        HelloMsg helloMsg = new HelloMsg(
                InetAddress.getLocalHost().getHostAddress(),
                "这是来自 " + clientType + " 客户端的消息!!"
        );
        String msg = helloService.sayHello(helloMsg);
        System.out.println(msg);
        
        GreetingFileService greetingFileService = proxy.getProxy(GreetingFileService.class);
        String fileName = "greeting-" + clientType + ".txt";
        String name = clientType + "-Hanfei";
        String address = InetAddress.getLocalHost().getHostAddress();
        System.out.println("address: " + address);
        String result = greetingFileService.createGreetingFileToServer(name, address, fileName);
        System.out.println(result);
    }
}
