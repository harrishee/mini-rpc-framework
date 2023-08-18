package com.hanfei.rpc.exception;

import com.hanfei.rpc.enumeration.RpcErrorMsgEnum;

/**
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class RpcRuntimeException extends RuntimeException {

    /**
     * 使用指定的错误枚举和详细信息创建 RpcRuntimeException 实例
     *
     * @param error  错误枚举，表示异常的类型
     * @param detail 详细信息，描述异常的具体情况
     */
    public RpcRuntimeException(RpcErrorMsgEnum error, String detail) {
        super(error.getMessage() + ": " + detail);
    }


    /**
     * 使用指定的错误消息和原因创建 RpcRuntimeException 实例
     *
     * @param message 错误消息，描述异常的简要信息
     * @param cause   异常的原因，通常是其他异常实例
     */
    public RpcRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }


    /**
     * 使用指定的错误枚举创建 RpcRuntimeException 实例
     *
     * @param error 错误枚举，表示异常的类型
     */
    public RpcRuntimeException(RpcErrorMsgEnum error) {
        super(error.getMessage());
    }
}
