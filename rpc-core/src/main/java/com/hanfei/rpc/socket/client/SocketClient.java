package com.hanfei.rpc.socket.client;

import com.hanfei.rpc.RpcClient;
import com.hanfei.rpc.entity.RpcRequest;
import com.hanfei.rpc.entity.RpcResponse;
import com.hanfei.rpc.enums.ErrorEnum;
import com.hanfei.rpc.enums.ResponseEnum;
import com.hanfei.rpc.exception.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * 客户端的 Socket 通信实现
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class SocketClient implements RpcClient {

    private static final Logger logger = LoggerFactory.getLogger(SocketClient.class);

    private final String host;

    private final int port;

    /**
     * 构造函数，传入服务器的主机名和端口号
     */
    public SocketClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * 发送 RPC 请求并返回调用结果
     */
    @Override
    public Object sendRequest(RpcRequest rpcRequest) {
        // 创建 Socket 连接到指定地址和端口
        try (Socket socket = new Socket(host, port)) {
            logger.info("Socket 客户端连接到服务器 {}:{}", host, port);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

            // 发送 RPC 请求对象到服务器
            objectOutputStream.writeObject(rpcRequest);
            objectOutputStream.flush();

            // 从服务器接收 RPC 响应对象
            RpcResponse rpcResponse = (RpcResponse) objectInputStream.readObject();

            // 检查响应对象
            if (rpcResponse == null) {
                logger.error("服务调用失败，service：{}", rpcRequest.getInterfaceName());
                throw new RpcException(ErrorEnum.SERVICE_INVOCATION_FAILURE, " service:" + rpcRequest.getInterfaceName());
            }

            // 检查响应状态码
            if (rpcResponse.getStatusCode() == null || rpcResponse.getStatusCode() != ResponseEnum.SUCCESS.getCode()) {
                logger.error("调用服务失败, service: {}, response: {}", rpcRequest.getInterfaceName(), rpcResponse);
                throw new RpcException(ErrorEnum.SERVICE_INVOCATION_FAILURE, " service:" + rpcRequest.getInterfaceName());
            }

            // 返回 RPC 响应中的数据部分
            return rpcResponse.getData();
        } catch (IOException | ClassNotFoundException e) {
            logger.error("调用时有错误发生：", e);
            throw new RpcException("服务调用失败: ", e);
        }
    }
}
