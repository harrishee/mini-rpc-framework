package com.hanfei.server;

import com.hanfei.rpc.anno.ServiceScan;
import com.hanfei.rpc.transport.RpcServer;
import com.hanfei.rpc.transport.server.NettyServer;

@ServiceScan
public class NettyRpcServer {
    public static void main(String[] args) {
        RpcServer nettyServer = new NettyServer("127.0.0.1", 9999);
        nettyServer.start();
    }
}
