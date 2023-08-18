package com.hanfei.rpc.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * 用于接收客户端请求并分配线程进行处理
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class RpcServer {

    // 用于处理客户端请求的线程池
    private final ExecutorService threadPool;

    private static final Logger logger = LoggerFactory.getLogger(RpcServer.class);


    /**
     * 构造函数，初始化线程池
     */
    public RpcServer() {
        int corePoolSize = 5; // 核心线程数
        int maximumPoolSize = 50; // 最大线程数
        long keepAliveTime = 60; // 线程空闲时间（单位：秒）

        // 创建线程池，使用阻塞队列作为任务队列
        BlockingQueue<Runnable> workingQueue = new ArrayBlockingQueue<>(100);
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        threadPool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, workingQueue, threadFactory);
    }

    /**
     * 注册服务对象并监听指定端口，接收客户端请求
     *
     * @param service 要注册的服务对象
     * @param port 监听的端口号
     */
    public void register(Object service, int port) {
        System.out.println("****RpcServer** register: " + service + " port: " + port);
        // 创建了一个 ServerSocket 对象，用于监听指定端口上的客户端连接请求
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("Starting server...");
            Socket socket;

            // 通过 while 循环来不断接收客户端的请求
            while ((socket = serverSocket.accept()) != null) {
                logger.info("Client connected！IP: " + socket.getInetAddress());
                // 创建 WorkerThread 来处理客户端请求，并将其提交到线程池中执行
                threadPool.execute(new WorkerThread(socket, service));
            }
        } catch (IOException e) {
            logger.error("Error when connecting: ", e);
        }
    }
}
