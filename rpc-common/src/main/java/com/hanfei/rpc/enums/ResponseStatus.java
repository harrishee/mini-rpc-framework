package com.hanfei.rpc.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseStatus {
    SUCCESS(200, "方法调用成功"),
    ERROR(500, "方法调用失败"),
    METHOD_NOT_FOUND(500, "方法未找到"),
    CLASS_NOT_FOUND(500, "类未找到"),
    SERVICE_NOT_FOUND(500, "服务未找到");

    private final int code;
    private final String msg;
}
