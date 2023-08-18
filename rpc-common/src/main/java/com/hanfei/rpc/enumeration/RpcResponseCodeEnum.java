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
public enum RpcResponseCodeEnum {

    SUCCESS(200, "调用方法成功"),

    ERROR(500, "调用方法失败"),

    METHOD_NOT_FOUND(500, "未找到指定方法"),

    CLASS_NOT_FOUND(500, "未找到指定类");

    private final int code;

    private final String message;
}
