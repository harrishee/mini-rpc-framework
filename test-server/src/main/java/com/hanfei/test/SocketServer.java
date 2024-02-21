package com.hanfei.test;

import com.hanfei.rpc.annotation.ServiceScan;
import com.hanfei.rpc.serialize.CommonSerializer;
import com.hanfei.rpc.transport.RpcServer;


@ServiceScan
public class SocketServer {

    public static void main(String[] args) {
        RpcServer socketServer = new com.hanfei.rpc.transport.socket.server.SocketServer("127.0.0.1", 9998, CommonSerializer.KRYO_SERIALIZER);
        socketServer.start();
    }
}
