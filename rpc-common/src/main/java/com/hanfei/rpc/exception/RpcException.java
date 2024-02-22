package com.hanfei.rpc.exception;

import com.hanfei.rpc.enums.ErrorEnum;

public class RpcException extends RuntimeException {
    public RpcException(ErrorEnum error, String detail) {
        super(error.getMessage() + ": " + detail);
    }

    public RpcException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public RpcException(ErrorEnum error) {
        super(error.getMessage());
    }
}
