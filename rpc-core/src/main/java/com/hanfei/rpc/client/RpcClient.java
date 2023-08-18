package com.hanfei.rpc.client;

import com.hanfei.rpc.entity.RpcRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * 用于发送远程调用请求并接收响应
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class RpcClient {

    private static final Logger logger = LoggerFactory.getLogger(RpcClient.class);

    public Object sendRequest(RpcRequest rpcRequest, String host, int port) {
        try (Socket socket = new Socket(host, port)) {
            // 创建对象输出流将 RpcRequest 对象序列化并发送到服务端
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            // 创建对象输入流用于接收服务端返回的响应数据
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

            // 将 RpcRequest 对象写入对象输出流并刷新
            objectOutputStream.writeObject(rpcRequest);
            objectOutputStream.flush();

            // 从对象输入流中读取服务端返回的响应数据并进行反序列化
            return objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            // 捕获可能发生的 IO 异常和类未找到异常，并记录错误日志
            logger.error("调用时有错误发生：", e);
            return null;
        }
    }
}
