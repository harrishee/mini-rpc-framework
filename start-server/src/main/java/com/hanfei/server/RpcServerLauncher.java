package com.hanfei.server;

import com.hanfei.rpc.anno.ServiceScan;
import com.hanfei.rpc.transport.RpcServer;
import com.hanfei.rpc.transport.server.NettyServer;
import com.hanfei.rpc.transport.server.SocketServer;

@ServiceScan
public class RpcServerLauncher {
    public static void main(String[] args) {
        RpcServer server;
        String serverType = System.getProperty("serverType", "netty");
        String host = System.getProperty("serverHost", "127.0.0.1");
        int port = Integer.parseInt(System.getProperty("serverPort", "9999"));
        
        switch (serverType) {
            case "netty":
                server = new NettyServer(host, port);
                break;
            case "socket":
                server = new SocketServer(host, port);
                break;
            default:
                throw new IllegalArgumentException("未知的服务端类型: " + serverType);
        }
        
        server.start();
    }
}
