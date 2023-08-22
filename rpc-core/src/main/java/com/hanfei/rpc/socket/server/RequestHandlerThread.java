package com.hanfei.rpc.socket.server;

import com.hanfei.rpc.RequestHandler;
import com.hanfei.rpc.entity.RpcRequest;
import com.hanfei.rpc.entity.RpcResponse;
import com.hanfei.rpc.registry.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * 处理 请求对象 的工作线程
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class RequestHandlerThread implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(RequestHandlerThread.class);

    /**
     * 用于与客户端通信的 Socket 对象
     */
    private Socket socket;

    /**
     * 处理请求的 请求处理器
     */
    private RequestHandler requestHandler;

    /**
     * 服务注册表，用于获取服务实例
     */
    private ServiceRegistry serviceRegistry;

    /**
     * 构造函数，初始化线程参数
     */
    public RequestHandlerThread(Socket socket, RequestHandler requestHandler, ServiceRegistry serviceRegistry) {
        this.socket = socket;
        this.requestHandler = requestHandler;
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * 线程运行方法，处理客户端请求并返回响应
     */
    @Override
    public void run() {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())) {
            // 从输入流读取客户端发送的请求对象，获得接口名
            RpcRequest rpcRequest = (RpcRequest) objectInputStream.readObject();
            String interfaceName = rpcRequest.getInterfaceName();
            logger.info("1. 服务器获得接口名: {}", interfaceName);

            // 根据接口名获取对应的服务实体
            Object service = serviceRegistry.getService(interfaceName);
            logger.info("2. 服务器获得服务实体: {}", service);

            // 调用 请求处理器 处理客户端请求，得到执行结果
            Object result = requestHandler.handle(rpcRequest, service);
            logger.info("3. 服务器获得执行结果: {}", result);

            // 将执行结果封装成响应对象并通过输出流发送给客户端
            logger.info("4. 服务器将执行结果封装成响应对象并通过输出流发送给客户端...");
            objectOutputStream.writeObject(RpcResponse.success(result));
            objectOutputStream.flush();
        } catch (IOException | ClassNotFoundException e) {
            logger.error("调用或发送时有错误发生：", e);
        }
    }
}
