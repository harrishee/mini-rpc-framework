package com.hanfei.rpc.server;

import com.hanfei.rpc.entity.RpcRequest;
import com.hanfei.rpc.entity.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

/**
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class WorkerThread implements Runnable {

    private Socket socket;

    private Object service;

    private static final Logger logger = LoggerFactory.getLogger(WorkerThread.class);

    /**
     * 构造函数，初始化 WorkerThread
     */
    public WorkerThread(Socket socket, Object service) {
        this.socket = socket;
        this.service = service;
    }

    /**
     * 线程运行方法，处理客户端请求
     */
    @Override
    public void run() {
        // 创建了一个 ObjectInputStream 对象，用于从套接字（Socket）的输入流中读取对象数据
        // 创建了一个 ObjectOutputStream 对象，用于将对象数据写入到套接字（Socket）的输出流中
        try (ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())) {

            // 从客户端的输入流中读取 RpcRequest 请求对象
            RpcRequest rpcRequest = (RpcRequest) objectInputStream.readObject();

            // 获取客户端请求的方法名和参数类型
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());

            // 通过反射调用服务端的方法，传入参数，获取方法执行结果
            Object returnObject = method.invoke(service, rpcRequest.getParameters());

            // 将方法执行结果封装成 RpcResponse 对象并写入输出流，返回给客户端
            objectOutputStream.writeObject(RpcResponse.success(returnObject));
            objectOutputStream.flush();
        } catch (IOException | ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                 InvocationTargetException e) {
            logger.error("调用或发送时有错误发生：", e);
        }
    }
}
