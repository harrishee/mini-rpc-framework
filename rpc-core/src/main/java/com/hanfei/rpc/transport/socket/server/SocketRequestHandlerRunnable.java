package com.hanfei.rpc.transport.socket.server;

import com.hanfei.rpc.entity.RpcRequest;
import com.hanfei.rpc.entity.RpcResponse;
import com.hanfei.rpc.transport.handler.RpcRequestHandler;
import com.hanfei.rpc.serialize.CommonSerializer;
import com.hanfei.rpc.transport.socket.utils.ObjectReader;
import com.hanfei.rpc.transport.socket.utils.ObjectWriter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * handle request
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
@Slf4j
public class SocketRequestHandlerRunnable implements Runnable {

    private Socket socket;

    private RpcRequestHandler rpcRequestHandler;

    private CommonSerializer serializer;

    public SocketRequestHandlerRunnable(Socket socket, RpcRequestHandler rpcRequestHandler, CommonSerializer serializer) {
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
            RpcRequest rpcRequest = (RpcRequest) ObjectReader.getObjectFromInStream(inputStream);
            Object result = rpcRequestHandler.handleRequest(rpcRequest);
            RpcResponse<Object> response = RpcResponse.success(rpcRequest.getRequestId(), result);

            // write the result to the output stream
            ObjectWriter.writeObject(outputStream, response, serializer);
        } catch (IOException e) {
            log.error("Error when handling RPC request: {}", e.getMessage());
        }
    }
}
