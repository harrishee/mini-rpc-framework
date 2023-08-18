package com.hanfei.rpc.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
@Getter
@AllArgsConstructor
public enum RpcErrorMsgEnum {

    SERVICE_INVOCATION_FAILURE("服务调用出现失败"),

    SERVICE_NOT_FOUND("找不到对应的服务"),

    SERVICE_NOT_IMPLEMENT_ANY_INTERFACE("注册的服务未实现接口");

    private final String message;
}
