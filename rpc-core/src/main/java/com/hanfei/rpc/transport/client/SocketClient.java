package com.hanfei.rpc.transport.client;

import com.alibaba.nacos.api.remote.response.ResponseCode;
import com.hanfei.rpc.model.RpcRequest;
import com.hanfei.rpc.model.RpcResponse;
import com.hanfei.rpc.enums.ErrorEnum;
import com.hanfei.rpc.exception.RpcException;
import com.hanfei.rpc.loadbalancer.LoadBalancer;
import com.hanfei.rpc.loadbalancer.RoundRobin;
import com.hanfei.rpc.registry.ServiceDiscovery;
import com.hanfei.rpc.registry.NacosServiceDiscovery;
import com.hanfei.rpc.serializer.Serializer;
import com.hanfei.rpc.transport.RpcClient;
import com.hanfei.rpc.transport.codec.SocketDecoder;
import com.hanfei.rpc.transport.codec.SocketEncoder;
import com.hanfei.rpc.util.MessageUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

@Slf4j
public class SocketClient implements RpcClient {
    private final Serializer serializer;
    private final ServiceDiscovery serviceDiscovery;
    
    public SocketClient(Integer serializerCode) {
        this(serializerCode, new RoundRobin());
    }
    
    public SocketClient(Integer serializerCode, LoadBalancer loadBalancer) {
        this.serializer = Serializer.getSerializer(serializerCode);
        this.serviceDiscovery = new NacosServiceDiscovery(loadBalancer);
    }
    
    @Override
    public Object sendRequest(RpcRequest rpcRequest) {
        // 通过服务发现获取服务提供方的地址
        InetSocketAddress inetSocketAddress = serviceDiscovery.discoverService(rpcRequest.getInterfaceName());
        try (Socket socket = new Socket()) {
            // 创建Socket连接到服务提供方
            socket.connect(inetSocketAddress);
            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = socket.getInputStream();
            
            // 序列化请求对象并通过Socket输出流发送给服务提供方
            SocketEncoder.serializeAndWrite(outputStream, rpcRequest, serializer);
            
            // **** 这时候服务提供方已经接收到了请求，开始处理请求并返回响应
            
            // 从Socket输入流读取响应数据并反序列化成RpcResponse对象
            Object obj = SocketDecoder.readAndDeserialize(inputStream);
            RpcResponse rpcResponse = (RpcResponse) obj;
            
            // 检查响应是否为空或状态码是否表示成功
            if (rpcResponse == null || rpcResponse.getStatusCode() == null || rpcResponse.getStatusCode() != ResponseCode.SUCCESS.getCode()) {
                log.error("服务调用失败, service: {}, response:{}", rpcRequest.getInterfaceName(), rpcResponse);
                throw new RpcException(ErrorEnum.SERVICE_INVOCATION_FAILURE, "服务调用失败: service:" + rpcRequest.getInterfaceName());
            }
            
            // 验证响应与请求是否匹配，并返回响应数据
            MessageUtil.validate(rpcRequest, rpcResponse);
            return rpcResponse;
        } catch (IOException e) {
            log.error("调用时有错误发生: ", e);
            throw new RpcException("服务调用失败: ", e);
        }
    }
}
