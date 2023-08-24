package com.hanfei.rpc.transport.socket.server;

import com.hanfei.rpc.factory.ThreadPoolFactory;
import com.hanfei.rpc.handler.RequestHandler;
import com.hanfei.rpc.hook.ShutdownHook;
import com.hanfei.rpc.provider.ServiceProviderImpl;
import com.hanfei.rpc.registry.nacos.NacosServiceRegistry;
import com.hanfei.rpc.serialize.CommonSerializer;
import com.hanfei.rpc.transport.AbstractRpcServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

/**
 * Socket 服务器实现类
 * 通过监听端口来接受客户端的请求，并将其分发到线程池中进行处理
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class SocketServer extends AbstractRpcServer {

    private final ExecutorService threadPool;

    private final CommonSerializer serializer;

    private final RequestHandler requestHandler = new RequestHandler();

    public SocketServer(String host, int port, Integer serializer) {
        this.host = host;
        this.port = port;
        this.serviceRegistry = new NacosServiceRegistry();
        this.serviceProvider = new ServiceProviderImpl();
        threadPool = ThreadPoolFactory.createDefaultThreadPool("socket-rpc-server");
        this.serializer = CommonSerializer.getByCode(serializer);

        serviceScan();
    }

    @Override
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket()) {
            serverSocket.bind(new InetSocketAddress(host, port));
            logger.info("服务器启动……");
            ShutdownHook.getShutdownHook().addClearAllHook();
            Socket socket;
            while ((socket = serverSocket.accept()) != null) {
                logger.info("消费者连接: {}:{}", socket.getInetAddress(), socket.getPort());
                threadPool.execute(new SocketRequestHandlerRunnable(socket, requestHandler, serializer));
            }
            threadPool.shutdown();
        } catch (IOException e) {
            logger.error("服务器启动时有错误发生:", e);
        }
    }
}
