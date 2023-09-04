package com.hanfei.test.serviceImpl;

import com.hanfei.rpc.annotation.Service;
import com.hanfei.rpc.api.HelloPOJO;
import com.hanfei.rpc.api.HelloService;

/**
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
@Service
public class HelloServiceImpl implements HelloService {

    @Override
    public String sayHello(HelloPOJO helloPOJO) {
        String serverName = helloPOJO.getServerName();
        String clientName = helloPOJO.getClientName();
        String message = helloPOJO.getMessage();

        return "clientName: [" + clientName + "], serverName: [" + serverName + "], message: [" + message + "]";
    }
}
