package com.hanfei.rpc.transport.server;

import com.hanfei.rpc.model.RpcRequest;
import com.hanfei.rpc.model.RpcResponse;
import com.hanfei.rpc.serializer.Serializer;
import com.hanfei.rpc.transport.codec.SocketEncoder;
import com.hanfei.rpc.transport.codec.SocketDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

@Slf4j
public class SocketRequestHandler implements Runnable {
    private final Socket socket;
    private final Serializer serializer;
    private final RpcRequestHandler rpcRequestHandler;
    
    public SocketRequestHandler(Socket socket, RpcRequestHandler rpcRequestHandler, Serializer serializer) {
        this.socket = socket;
        this.serializer = serializer;
        this.rpcRequestHandler = rpcRequestHandler;
    }
    
    @Override
    public void run() {
        // 从Socket获取输入流，用于接收客户端发送的请求数据
        // 从Socket获取输出流，用于向客户端发送响应数据
        try (InputStream inputStream = socket.getInputStream();
             OutputStream outputStream = socket.getOutputStream()) {
            
            // 从输入流中读取数据，并反序列化为RpcRequest对象
            RpcRequest rpcRequest = (RpcRequest) SocketDecoder.readAndDeserialize(inputStream);
            
            // 调用RpcRequestHandler处理RPC请求，获取处理结果
            RpcResponse<?> response = rpcRequestHandler.processRequest(rpcRequest);
            
            // 将处理结果序列化并写入输出流，返回给客户端
            SocketEncoder.serializeAndWrite(outputStream, response, serializer);
        } catch (IOException e) {
            log.error("Socket服务器数据处理器，发生异常: ", e);
        }
    }
}
