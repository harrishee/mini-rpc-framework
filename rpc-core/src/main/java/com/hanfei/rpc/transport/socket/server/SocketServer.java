package com.hanfei.rpc.transport.socket.server;

import com.hanfei.rpc.handler.RequestHandler;
import com.hanfei.rpc.transport.RpcServer;
import com.hanfei.rpc.enums.ErrorEnum;
import com.hanfei.rpc.exception.RpcException;
import com.hanfei.rpc.provider.ServiceProvider;
import com.hanfei.rpc.provider.ServiceProviderImpl;
import com.hanfei.rpc.registry.NacosServiceRegistry;
import com.hanfei.rpc.registry.ServiceRegistry;
import com.hanfei.rpc.serializer.CommonSerializer;
import com.hanfei.rpc.util.ThreadPoolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class SocketServer implements RpcServer {

    private static final Logger logger = LoggerFactory.getLogger(SocketServer.class);

    // 线程池实例
    private final ExecutorService threadPool;

    // 请求处理器
    private RequestHandler requestHandler = new RequestHandler();

    // 服务注册表
    private final ServiceProvider serviceProvider;

    // 服务注册中心
    private final ServiceRegistry serviceRegistry;

    // 服务器主机名
    private final String host;

    // 服务器端口号
    private final int port;

    // 序列化器
    private CommonSerializer serializer;

    /**
     * 构造函数
     */
    public SocketServer(String host, int port) {
        this.host = host;
        this.port = port;
        threadPool = ThreadPoolFactory.createDefaultThreadPool("socket-rpc-server");
        this.serviceRegistry = new NacosServiceRegistry();
        this.serviceProvider = new ServiceProviderImpl();
    }

    /**
     * 启动服务器，监听指定端口
     */
    @Override
    public void start() {
        // 创建 ServerSocket 对象并绑定到指定端口
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("Socket Server Starts...");
            Socket socket;

            // 持续监听客户端连接
            while ((socket = serverSocket.accept()) != null) {
                logger.info("Socket Client Connected: {}:{}", socket.getInetAddress(), socket.getPort());

                // 在线程池中执行请求处理线程
                logger.info("开始执行请求处理线程...");
                threadPool.execute(new RequestHandlerThread(socket, requestHandler, serviceRegistry, serializer));
            }
            // 关闭线程池
            threadPool.shutdown();
        } catch (IOException e) {
            logger.error("服务器启动时有错误发生:", e);
        }
    }

    @Override
    public void setSerializer(CommonSerializer serializer) {
        this.serializer = serializer;
    }

    @Override
    public <T> void publishService(Object service, Class<T> serviceClass) {
        if (serializer == null) {
            logger.error("未设置序列化器");
            throw new RpcException(ErrorEnum.SERIALIZER_NOT_FOUND);
        }
        serviceProvider.addServiceProvider(service);
        serviceRegistry.register(serviceClass.getCanonicalName(), new InetSocketAddress(host, port));
        start();
    }
}
