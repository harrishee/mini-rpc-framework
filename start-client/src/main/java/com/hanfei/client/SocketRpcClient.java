package com.hanfei.client;

import com.hanfei.api.GreetingFileService;
import com.hanfei.api.HelloMsg;
import com.hanfei.api.HelloService;
import com.hanfei.rpc.serializer.Serializer;
import com.hanfei.rpc.transport.RpcClientProxy;
import com.hanfei.rpc.transport.client.SocketClient;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class SocketRpcClient {
    public static void main(String[] args) throws UnknownHostException {
        SocketClient client = new SocketClient(Serializer.KRYO_SERIALIZER);
        RpcClientProxy proxy = new RpcClientProxy(client);
        
        HelloService helloService = proxy.getProxy(HelloService.class);
        HelloMsg helloMsg = new HelloMsg(
                InetAddress.getLocalHost().getHostAddress(),
                "This is the 1st from SocketRpcClient!!"
        );
        String msg = helloService.sayHello(helloMsg);
        System.out.println(msg);
        
        HelloMsg helloMsg2 = new HelloMsg(
                InetAddress.getLocalHost().getHostAddress(),
                "This is the 2nd msg from SocketRpcClient!!"
        );
        String msg2 = helloService.sayHello(helloMsg2);
        System.out.println(msg2);
        
        GreetingFileService greetingFileService = proxy.getProxy(GreetingFileService.class);
        String fileName = "greeting-socket.txt";
        String name = "SocketRpcClient-Hanfei";
        String address = InetAddress.getLocalHost().getHostAddress();
        String result = greetingFileService.createGreetingFileToServer(name, address, fileName);
        System.out.println(result);
    }
}
