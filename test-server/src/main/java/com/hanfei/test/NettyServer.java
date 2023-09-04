package com.hanfei.test;

import com.hanfei.rpc.annotation.ServiceScan;
import com.hanfei.rpc.serialize.CommonSerializer;
import com.hanfei.rpc.transport.RpcServer;

/**
 * Server side, Netty Version
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
@ServiceScan
public class NettyServer {

    public static void main(String[] args) {
        RpcServer nettyServer = new com.hanfei.rpc.transport.netty.server.NettyServer("127.0.0.1", 9999, CommonSerializer.KRYO_SERIALIZER);
        nettyServer.start();
    }
}
