package com.hanfei.rpc.transport.socket.server;

import com.hanfei.rpc.entity.RpcRequest;
import com.hanfei.rpc.entity.RpcResponse;
import com.hanfei.rpc.handler.RequestHandler;
import com.hanfei.rpc.registry.ServiceRegistry;
import com.hanfei.rpc.serializer.CommonSerializer;
import com.hanfei.rpc.transport.socket.utils.ObjectReader;
import com.hanfei.rpc.transport.socket.utils.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    // 用于与客户端通信的 Socket 对象
    private Socket socket;

    // 处理请求的 请求处理器
    private RequestHandler requestHandler;

    // 服务注册表
    private ServiceRegistry serviceRegistry;

    // 序列化器
    private CommonSerializer serializer;

    /**
     * 构造函数，初始化线程参数
     */
    public RequestHandlerThread(Socket socket, RequestHandler requestHandler,
                                ServiceRegistry serviceRegistry, CommonSerializer serializer) {
        this.socket = socket;
        this.requestHandler = requestHandler;
        this.serviceRegistry = serviceRegistry;
        this.serializer = serializer;
    }

    /**
     * 线程运行方法，处理客户端请求并返回响应
     */
    @Override
    public void run() {
        try (InputStream inputStream = socket.getInputStream();
             OutputStream outputStream = socket.getOutputStream()) {
            // 从输入流中读取客户端发送的请求对象
            RpcRequest rpcRequest = (RpcRequest) ObjectReader.readObject(inputStream);
            // 获取请求中的接口名称
            String interfaceName = rpcRequest.getInterfaceName();
            // 处理请求并获取处理结果
            Object result = requestHandler.handle(rpcRequest);
            // 创建 RPC 响应对象，并将处理结果填入
            RpcResponse<Object> response = RpcResponse.success(result, rpcRequest.getRequestId());
            // 将响应对象序列化并写入输出流，返回给客户端
            ObjectWriter.writeObject(outputStream, response, serializer);
        } catch (IOException e) {
            logger.error("调用或发送时有错误发生：", e);
        }
    }
}
