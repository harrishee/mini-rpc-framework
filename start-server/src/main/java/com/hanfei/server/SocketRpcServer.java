package com.hanfei.server;

import com.hanfei.rpc.anno.ServiceScan;
import com.hanfei.rpc.transport.RpcServer;
import com.hanfei.rpc.transport.server.SocketServer;

@ServiceScan
public class SocketRpcServer {
    public static void main(String[] args) {
        RpcServer socketServer = new SocketServer("127.0.0.1", 9998);
        socketServer.start();
    }
}
