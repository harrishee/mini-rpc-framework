package com.hanfei.rpc.transport.socket.server;

import com.hanfei.rpc.factory.ThreadPoolFactory;
import com.hanfei.rpc.transport.handler.RpcRequestHandler;
import com.hanfei.rpc.hook.ShutdownHook;
import com.hanfei.rpc.provider.ServiceProviderImpl;
import com.hanfei.rpc.registry.nacos.NacosServiceRegistry;
import com.hanfei.rpc.serialize.CommonSerializer;
import com.hanfei.rpc.transport.AbstractRpcServer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

/**
 * Socket server
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
@Slf4j
public class SocketServer extends AbstractRpcServer {

    private final ExecutorService threadPool;

    private final CommonSerializer serializer;

    private final RpcRequestHandler rpcRequestHandler = new RpcRequestHandler();

    public SocketServer(String host, int port, Integer serializer) {
        this.host = host;
        this.port = port;
        this.serializer = CommonSerializer.getByCode(serializer);

        this.serviceRegistry = new NacosServiceRegistry();
        this.serviceProvider = new ServiceProviderImpl();
        threadPool = ThreadPoolFactory.createDefaultThreadPool("socket-rpc-server");

        serviceScan();
    }

    @Override
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket()) {
            serverSocket.bind(new InetSocketAddress(host, port));
            ShutdownHook.getShutdownHook().addClearAllHook();
            log.info("Server started successfully...");

            Socket socket;
            while ((socket = serverSocket.accept()) != null) {
                log.info("Client connected! {}:{}", socket.getInetAddress(), socket.getPort());

                // submit the task to the thread pool
                threadPool.execute(new SocketRequestHandlerRunnable(socket, rpcRequestHandler, serializer));
            }
            threadPool.shutdown();
        } catch (IOException e) {
            log.error("Error when starting socket server: {}", e.getMessage());
        }
    }
}
