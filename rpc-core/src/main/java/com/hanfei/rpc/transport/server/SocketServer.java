package com.hanfei.rpc.transport.server;

import com.hanfei.rpc.util.ThreadPoolFactory;
import com.hanfei.rpc.util.JVMUtil;
import com.hanfei.rpc.provider.LocalServiceProvider;
import com.hanfei.rpc.registry.NacosServiceRegistry;
import com.hanfei.rpc.serializer.Serializer;
import com.hanfei.rpc.transport.RpcServerBase;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

@Slf4j
public class SocketServer extends RpcServerBase {
    private final ExecutorService threadPool; // 线程池，用于处理客户端请求（一个TCP连接的建立和数据的发送）
    private final RpcRequestHandler rpcRequestHandler = new RpcRequestHandler();
    
    public SocketServer(String host, int port) {
        this(host, port, Serializer.DEFAULT_SERIALIZER);
    }
    
    public SocketServer(String host, int port, Integer serializerCode) {
        this.host = host;
        this.port = port;
        this.serializer = Serializer.getSerializer(serializerCode);
        this.serviceProvider = new LocalServiceProvider();
        this.serviceRegistry = new NacosServiceRegistry();
        
        // 线程池创建：通过ThreadPoolFactory.createDefaultThreadPool("socket-rpc-server")创建一个默认的线程池
        // 线程池用于并发处理客户端的请求，每接收到一个新的客户端连接，就从线程池中分配一个线程去处理，以提高服务处理的效率和并发能力
        this.threadPool = ThreadPoolFactory.createDefaultThreadPool("socket-rpc-server");
        
        serviceScan();
    }
    
    @Override
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket()) {
            serverSocket.bind(new InetSocketAddress(host, port));
            JVMUtil.addShutdownHook();
            log.info("Socket服务器，启动成功，监听地址：{} : {}", host, port);
            
            while (true) {
                Socket socket = serverSocket.accept(); // 阻塞等待客户端连接
                log.info("Socket服务器，收到新的连接：[{} : {}]", socket.getInetAddress(), socket.getPort());
                
                // 将接收到的客户端连接作为任务提交给线程池处理
                threadPool.execute(new SocketRequestHandler(socket, rpcRequestHandler, serializer));
            }
            
        } catch (IOException e) {
            log.error("Socket服务器，启动失败: ", e);
        } finally {
            threadPool.shutdown();
        }
    }
}
