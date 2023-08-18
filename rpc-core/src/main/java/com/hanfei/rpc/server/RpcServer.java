package com.hanfei.rpc.server;

import com.hanfei.rpc.registry.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * 用于接收客户端请求并分配线程进行处理的 RPC 服务器类
 * 此类负责启动服务器，接收客户端请求，并将请求分发给工作线程进行处理
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class RpcServer {

    // 用于处理客户端请求的线程池
    private final ExecutorService threadPool;

    // 线程池的核心线程数
    private static final int CORE_POOL_SIZE = 5;

    // 线程池的最大线程数
    private static final int MAXIMUM_POOL_SIZE = 50;

    // 线程池中空闲线程的存活时间
    private static final int KEEP_ALIVE_TIME = 60;

    // 线程池任务队列的容量
    private static final int BLOCKING_QUEUE_CAPACITY = 100;

    // 请求处理器，负责处理客户端请求
    private RequestHandler requestHandler = new RequestHandler();

    // 服务注册表，用于获取服务实体
    private final ServiceRegistry serviceRegistry;

    private static final Logger logger = LoggerFactory.getLogger(RpcServer.class);


    /**
     * 构造函数，初始化线程池和服务注册表
     *
     * @param serviceRegistry 服务注册表，用于获取服务实体
     */
    public RpcServer(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        logger.info("***RpcServer*** 服务注册表初始化: {}", serviceRegistry.getClass().getCanonicalName());

        // 创建线程池，其中的工作线程会处理客户端请求
        BlockingQueue<Runnable> workingQueue = new ArrayBlockingQueue<>(BLOCKING_QUEUE_CAPACITY);
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        threadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS, workingQueue, threadFactory);
        logger.info("***RpcServer*** 线程池初始化: 核心线程数={}, 最大线程数={}, 线程存活时间={}秒, 队列容量={}",
                CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME, BLOCKING_QUEUE_CAPACITY);
    }

    /**
     * 启动 RPC 服务器并监听指定端口
     *
     * @param port 监听的端口号
     */
    public void start(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("Server Started……");
            Socket socket;
            while ((socket = serverSocket.accept()) != null) {
                logger.info("Client Connected: {}:{}", socket.getInetAddress(), socket.getPort());

                // 将客户端请求交给线程池的工作线程处理
                threadPool.execute(new RequestHandlerThread(socket, requestHandler, serviceRegistry));
            }
            // 关闭线程池，停止接受新的任务，并且等待已经提交的任务执行完成
            threadPool.shutdown();
        } catch (IOException e) {
            logger.error("Error when starting server:", e);
        }
    }
}
