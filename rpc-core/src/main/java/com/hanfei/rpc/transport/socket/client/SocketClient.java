package com.hanfei.rpc.transport.socket.client;

import com.alibaba.nacos.api.remote.response.ResponseCode;
import com.hanfei.rpc.model.RpcRequest;
import com.hanfei.rpc.model.RpcResponse;
import com.hanfei.rpc.enums.ErrorEnum;
import com.hanfei.rpc.exception.RpcException;
import com.hanfei.rpc.loadbalancer.LoadBalance;
import com.hanfei.rpc.loadbalancer.RoundRobin;
import com.hanfei.rpc.registry.ServiceDiscovery;
import com.hanfei.rpc.registry.NacosServiceDiscovery;
import com.hanfei.rpc.serializer.Serializer;
import com.hanfei.rpc.transport.RpcClient;
import com.hanfei.rpc.transport.socket.util.ObjectReadUtil;
import com.hanfei.rpc.transport.socket.util.ObjectWriteUtil;
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

    public SocketClient(Integer serializer) {
        this(serializer, new RoundRobin());
    }

    public SocketClient(Integer serializer, LoadBalance loadBalance) {
        this.serializer = Serializer.getByCode(serializer);
        this.serviceDiscovery = new NacosServiceDiscovery(loadBalance);
    }

    @Override
    public Object sendRequest(RpcRequest rpcRequest) {
        if (serializer == null) {
            log.error("未设置序列化器");
            throw new RpcException(ErrorEnum.SERIALIZER_NOT_FOUND);
        }

        // 查找服务地址
        InetSocketAddress inetSocketAddress = serviceDiscovery.getServerByService(rpcRequest.getInterfaceName());
        try (Socket socket = new Socket()) {
            // 连接服务器
            socket.connect(inetSocketAddress);
            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = socket.getInputStream();

            // 将请求对象序列化并写入输出流
            ObjectWriteUtil.writeObject(outputStream, rpcRequest, serializer);

            // 从输入流中读取响应对象并反序列化
            Object obj = ObjectReadUtil.getObjectFromInStream(inputStream);
            RpcResponse rpcResponse = (RpcResponse) obj;

            if (rpcResponse == null) {
                log.error("服务调用失败，service：{}", rpcRequest.getInterfaceName());
                throw new RpcException(ErrorEnum.SERVICE_INVOCATION_FAILURE, " service:" + rpcRequest.getInterfaceName());
            }
            if (rpcResponse.getStatusCode() == null || rpcResponse.getStatusCode() != ResponseCode.SUCCESS.getCode()) {
                log.error("调用服务失败, service: {}, response:{}", rpcRequest.getInterfaceName(), rpcResponse);
                throw new RpcException(ErrorEnum.SERVICE_INVOCATION_FAILURE, " service:" + rpcRequest.getInterfaceName());
            }

            // 检查响应与请求是否匹配
            MessageUtil.validate(rpcRequest, rpcResponse);
            return rpcResponse;
        } catch (IOException e) {
            log.error("调用时有错误发生：", e);
            throw new RpcException("服务调用失败: ", e);
        }
    }
}
