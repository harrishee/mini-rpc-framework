package com.hanfei.test.serviceImpl;

import com.hanfei.rpc.anno.Service;
import com.hanfei.api.HelloMsg;
import com.hanfei.api.HelloService;

@Service
public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(HelloMsg helloMsg) {
        String serverName = helloMsg.getServerName();
        String clientName = helloMsg.getClientName();
        String message = helloMsg.getMessage();

        return "clientName: [" + clientName + "], serverName: [" + serverName + "], message: [" + message + "]";
    }
}
