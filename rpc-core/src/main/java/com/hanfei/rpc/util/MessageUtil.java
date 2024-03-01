package com.hanfei.rpc.util;

import com.hanfei.rpc.model.RpcRequest;
import com.hanfei.rpc.model.RpcResponse;
import com.hanfei.rpc.enums.ErrorEnum;
import com.hanfei.rpc.enums.ResponseStatus;
import com.hanfei.rpc.exception.RpcException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageUtil {
    public static void validate(RpcRequest rpcRequest, RpcResponse rpcResponse) {
        // 获取请求中的接口名称，用于日志记录和错误信息。
        String interfaceName = rpcRequest.getInterfaceName();

        // 检查响应对象是否为空
        if (rpcResponse == null) {
            log.error("调用服务时出错: {}，响应为空", interfaceName);
            throw new RpcException(ErrorEnum.SERVICE_INVOCATION_FAILURE, "接口名称:" + interfaceName);
        }

        // 检查响应ID是否与请求ID匹配
        if (!rpcResponse.getRequestId().equals(rpcRequest.getRequestId())) {
            log.error("调用服务时出错: {}，请求ID不匹配", interfaceName);
            throw new RpcException(ErrorEnum.RESPONSE_NOT_MATCH, "接口名称:" + interfaceName);
        }

        // 检查响应状态码是否表示成功
        if (rpcResponse.getStatusCode() == null || !rpcResponse.getStatusCode().equals(ResponseStatus.SUCCESS.getCode())) {
            log.error("调用服务时出错: {}，响应状态码非成功", interfaceName);
            throw new RpcException(ErrorEnum.SERVICE_INVOCATION_FAILURE, "接口名称:" + interfaceName);
        }
    }
}
