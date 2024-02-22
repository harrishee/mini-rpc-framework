package com.hanfei.rpc.transport.socket.server;

import com.hanfei.rpc.model.RpcRequest;
import com.hanfei.rpc.model.RpcResponse;
import com.hanfei.rpc.transport.RpcRequestHandler;
import com.hanfei.rpc.serializer.Serializer;
import com.hanfei.rpc.transport.socket.util.ObjectReadUtil;
import com.hanfei.rpc.transport.socket.util.ObjectWriteUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

@Slf4j
public class SocketRequestHandler implements Runnable {
    private final Socket socket;
    private final RpcRequestHandler rpcRequestHandler;
    private final Serializer serializer;

    public SocketRequestHandler(Socket socket, RpcRequestHandler rpcRequestHandler, Serializer serializer) {
        this.socket = socket;
        this.rpcRequestHandler = rpcRequestHandler;
        this.serializer = serializer;
    }

    /**
     * handle request
     */
    @Override
    public void run() {
        try (InputStream inputStream = socket.getInputStream();
             OutputStream outputStream = socket.getOutputStream()) {
            // get the request, process it, and then return the result
            RpcRequest rpcRequest = (RpcRequest) ObjectReadUtil.getObjectFromInStream(inputStream);
            Object result = rpcRequestHandler.handleRequest(rpcRequest);
            RpcResponse<Object> response = RpcResponse.success(rpcRequest.getRequestId(), result);

            // write the result to the output stream
            ObjectWriteUtil.writeObject(outputStream, response, serializer);
        } catch (IOException e) {
            log.error("Error when handling RPC request: {}", e.getMessage());
        }
    }
}
