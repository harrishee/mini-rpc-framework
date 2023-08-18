package com.hanfei.rpc.entity;

import com.hanfei.rpc.enumeration.ResponseCode;
import lombok.Data;

import java.io.Serializable;

/**
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
@Data
public class RpcResponse<T> implements Serializable {

    private Integer statusCode;

    private String message;

    private T data;

    public static <T> RpcResponse<T> success(T data) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setStatusCode(ResponseCode.SUCCESS.getCode());
        response.setMessage(ResponseCode.SUCCESS.getMessage());
        response.setData(data);
        return response;
    }

    public static <T> RpcResponse<T> error(ResponseCode code) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setStatusCode(code.getCode());
        response.setMessage(code.getMessage());
        return response;
    }
}
