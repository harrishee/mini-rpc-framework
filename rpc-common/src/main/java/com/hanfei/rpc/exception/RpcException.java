package com.hanfei.rpc.exception;

import com.hanfei.rpc.enums.ErrorEnum;

/**
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class RpcException extends RuntimeException {

    public RpcException(ErrorEnum error, String detail) {
        super(error.getMessage() + ": " + detail);
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public RpcException(ErrorEnum error) {
        super(error.getMessage());
    }
}
