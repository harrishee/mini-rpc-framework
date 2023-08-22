package com.hanfei.rpc.socket.server;

import com.hanfei.rpc.RequestHandler;
import com.hanfei.rpc.RpcServer;
import com.hanfei.rpc.registry.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

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

    /**
     * 线程池配置
     */
    private static final int CORE_POOL_SIZE = 5;
    private static final int MAXIMUM_POOL_SIZE = 50;
    private static final int KEEP_ALIVE_TIME = 60;
    private static final int BLOCKING_QUEUE_CAPACITY = 100;

    /**
     * 线程池实例
     */
    private final ExecutorService threadPool;

    /**
     * 请求处理器
     */
    private RequestHandler requestHandler = new RequestHandler();

    /**
     * 服务注册表
     */
    private final ServiceRegistry serviceRegistry;

    /**
     * 构造函数
     */
    public SocketServer(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;

        // 创建阻塞队列，用于存储待执行任务
        BlockingQueue<Runnable> workingQueue = new ArrayBlockingQueue<>(BLOCKING_QUEUE_CAPACITY);

        // 创建线程工厂，用于创建线程实例
        ThreadFactory threadFactory = Executors.defaultThreadFactory();

        // 初始化线程池
        threadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME,
                TimeUnit.SECONDS, workingQueue, threadFactory);
    }

    /**
     * 启动服务器，监听指定端口
     */
    @Override
    public void start(int port) {
        // 创建 ServerSocket 对象并绑定到指定端口
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("Socket Server Starts...");
            Socket socket;

            // 持续监听客户端连接
            while ((socket = serverSocket.accept()) != null) {
                logger.info("Socket Client Connected: {}:{}", socket.getInetAddress(), socket.getPort());

                // 在线程池中执行请求处理线程
                logger.info("开始执行请求处理线程...");
                threadPool.execute(new RequestHandlerThread(socket, requestHandler, serviceRegistry));
            }

            // 关闭线程池
            threadPool.shutdown();
        } catch (IOException e) {
            logger.error("服务器启动时有错误发生:", e);
        }
    }
}
