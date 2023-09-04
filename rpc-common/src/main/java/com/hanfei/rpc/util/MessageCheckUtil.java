package com.hanfei.rpc.util;

import com.hanfei.rpc.entity.RpcRequest;
import com.hanfei.rpc.entity.RpcResponse;
import com.hanfei.rpc.enums.ErrorEnum;
import com.hanfei.rpc.enums.ResponseEnum;
import com.hanfei.rpc.exception.RpcException;
import lombok.extern.slf4j.Slf4j;

/**
 * validate if the response is correct
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
@Slf4j
public class MessageCheckUtil {

    private MessageCheckUtil() {
    }

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
