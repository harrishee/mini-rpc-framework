package com.hanfei.test;

import com.hanfei.rpc.anno.ServiceScan;
import com.hanfei.rpc.serializer.Serializer;
import com.hanfei.rpc.transport.RpcServer;
import com.hanfei.rpc.transport.netty.server.NettyServer;

@ServiceScan
public class NettyRpcServer {
    public static void main(String[] args) {
        RpcServer nettyServer = new NettyServer("127.0.0.1", 9999, Serializer.KRYO_SERIALIZER);
        nettyServer.start();
    }
}
