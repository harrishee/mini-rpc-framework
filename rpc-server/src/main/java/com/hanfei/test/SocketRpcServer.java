package com.hanfei.test;

import com.hanfei.rpc.anno.ServiceScan;
import com.hanfei.rpc.serializer.Serializer;
import com.hanfei.rpc.transport.RpcServer;
import com.hanfei.rpc.transport.socket.server.SocketServer;

@ServiceScan
public class SocketRpcServer {
    public static void main(String[] args) {
        RpcServer socketServer = new SocketServer("127.0.0.1", 9998, Serializer.KRYO_SERIALIZER);
        socketServer.start();
    }
}
