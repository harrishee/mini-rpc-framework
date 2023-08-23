package com.hanfei.test;

import com.hanfei.rpc.api.CalculateService;
import com.hanfei.rpc.serializer.CommonSerializer;
import com.hanfei.rpc.transport.netty.server.NettyServer;
import com.hanfei.test.ServiceImpl.CalculateServiceImpl;

/**
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class NettyTestServer {

    public static void main(String[] args) {
        // HelloService helloService = new HelloServiceImpl();
        CalculateService calculateService = new CalculateServiceImpl();

        NettyServer server = new NettyServer("127.0.0.1", 9999, CommonSerializer.KRYO_SERIALIZER);
        // server.publishService(helloService, HelloService.class);
        server.publishService(calculateService, CalculateService.class);
    }
}
