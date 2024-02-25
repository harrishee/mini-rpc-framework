package com.hanfei.server.service;

import com.hanfei.rpc.anno.Service;
import com.hanfei.api.HelloMsg;
import com.hanfei.api.HelloService;

@Service
public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(HelloMsg helloMsg) {
        String clientName = helloMsg.getClientName();
        String message = helloMsg.getMessage();

        return "Hello this is server! clientName: [" + clientName + "], message: [" + message + "]";
    }
}
