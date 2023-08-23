package com.hanfei.rpc.util;

import com.hanfei.rpc.entity.RpcRequest;
import com.hanfei.rpc.entity.RpcResponse;
import com.hanfei.rpc.enums.ErrorEnum;
import com.hanfei.rpc.enums.ResponseEnum;
import com.hanfei.rpc.exception.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 检查 RPC 消息的工具类，用于校验请求和响应是否合法
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class MessageCheckUtil {

    private static final Logger logger = LoggerFactory.getLogger(MessageCheckUtil.class);

    // 请求中接口名称的键
    public static final String INTERFACE_NAME = "interfaceName";

    // 私有构造函数，不允许实例化该工具类
    private MessageCheckUtil() {
    }

    /**
     * 校验 RPC 请求和响应是否合法
     */
    public static void check(RpcRequest rpcRequest, RpcResponse rpcResponse) {
        if (rpcResponse == null) {
            logger.error("调用服务失败，serviceName: {}", rpcRequest.getInterfaceName());
            throw new RpcException(ErrorEnum.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":"
                    + rpcRequest.getInterfaceName());
        }

        // 校验请求和响应的请求 ID 是否匹配
        if (!rpcRequest.getRequestId().equals(rpcResponse.getRequestId())) {
            logger.error("调用服务失败，serviceName: {}", rpcRequest.getInterfaceName());
            throw new RpcException(ErrorEnum.RESPONSE_NOT_MATCH, INTERFACE_NAME + ":"
                    + rpcRequest.getInterfaceName());
        }

        // 校验响应状态码是否为成功
        if (rpcResponse.getStatusCode() == null || !rpcResponse.getStatusCode().equals(ResponseEnum.SUCCESS.getCode())) {
            logger.error("调用服务失败，serviceName: {}，RpcResponse: {}", rpcRequest.getInterfaceName(), rpcResponse);
            throw new RpcException(ErrorEnum.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":"
                    + rpcRequest.getInterfaceName());
        }
    }
}
