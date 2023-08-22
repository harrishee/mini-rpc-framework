package com.hanfei.rpc.exception;

import com.hanfei.rpc.enums.ErrorEnum;

/**
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class RpcException extends RuntimeException {

    /**
     * 使用指定的错误枚举和详细信息创建异常实例
     */
    public RpcException(ErrorEnum error, String detail) {
        super(error.getMessage() + ": " + detail);
    }

    /**
     * 使用指定的错误消息和原因创建异常实例
     */
    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 使用指定的错误枚举创建异常实例
     */
    public RpcException(ErrorEnum error) {
        super(error.getMessage());
    }
}
