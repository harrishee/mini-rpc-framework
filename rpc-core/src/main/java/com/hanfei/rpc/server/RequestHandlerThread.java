package com.hanfei.rpc.server;

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
 * 处理 RpcRequest 的工作线程，用于处理客户端请求并返回响应
 * 此类负责从客户端接收请求，根据请求调用相应的服务方法，并将执行结果返回给客户端
 * 通过多线程处理，支持并发处理多个客户端请求
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class RequestHandlerThread implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(RequestHandlerThread.class);

    private Socket socket;

    // 用于处理请求的 RequestHandler 实例
    private RequestHandler requestHandler;

    private ServiceRegistry serviceRegistry;

    /**
     * 构造函数，初始化线程需要的参数
     *
     * @param socket           与客户端通信的 Socket 对象
     * @param requestHandler   处理请求的 RequestHandler 实例
     * @param serviceRegistry  服务注册表，用于获取服务实体
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
            // 从输入流读取客户端发送的 RpcRequest 对象
            RpcRequest rpcRequest = (RpcRequest) objectInputStream.readObject();
            String interfaceName = rpcRequest.getInterfaceName();

            // 根据接口名获取对应的服务实体
            Object service = serviceRegistry.getService(interfaceName);

            // 调用 RequestHandler 的 handle 方法，执行服务调用逻辑
            Object result = requestHandler.handle(rpcRequest, service);

            // 将执行结果封装成 RpcResponse 并通过输出流发送给客户端
            objectOutputStream.writeObject(RpcResponse.success(result));
            objectOutputStream.flush();
        } catch (IOException | ClassNotFoundException e) {
            logger.error("调用或发送时有错误发生：", e);
        }
    }
}
