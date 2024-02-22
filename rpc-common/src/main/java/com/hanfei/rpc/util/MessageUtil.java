package com.hanfei.rpc.util;

import com.hanfei.rpc.model.RpcRequest;
import com.hanfei.rpc.model.RpcResponse;
import com.hanfei.rpc.enums.ErrorEnum;
import com.hanfei.rpc.enums.ResponseEnum;
import com.hanfei.rpc.exception.RpcException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageUtil {
    public static void validate(RpcRequest rpcRequest, RpcResponse rpcResponse) {
        String interfaceName = rpcRequest.getInterfaceName();
        if (rpcResponse == null) {
            log.error("Error when calling service: {}，response is null", interfaceName);
            throw new RpcException(ErrorEnum.SERVICE_INVOCATION_FAILURE, "interfaceName:" + interfaceName);
        }

        if (!rpcResponse.getRequestId().equals(rpcRequest.getRequestId())) {
            log.error("Error when calling service: {}，not correct requestId", interfaceName);
            throw new RpcException(ErrorEnum.RESPONSE_NOT_MATCH, "interfaceName:" + interfaceName);
        }

        if (rpcResponse.getStatusCode() == null || !rpcResponse.getStatusCode().equals(ResponseEnum.SUCCESS.getCode())) {
            log.error("Error when calling service: {}，response status code not success", interfaceName);
            throw new RpcException(ErrorEnum.SERVICE_INVOCATION_FAILURE, "interfaceName:" + interfaceName);
        }
    }
}
