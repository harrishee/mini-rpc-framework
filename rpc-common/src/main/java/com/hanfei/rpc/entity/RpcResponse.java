package com.hanfei.rpc.entity;

import com.hanfei.rpc.enums.ResponseEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@NoArgsConstructor
public class RpcResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    // unique identifier for the corresponding request
    private String requestId;

    // response status code
    private Integer statusCode;

    // response message
    private String message;

    // response data
    private T data;

    // constructor for success response
    public static <T> RpcResponse<T> success(String requestId, T data) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setRequestId(requestId);
        response.setStatusCode(ResponseEnum.SUCCESS.getCode());
        response.setData(data);
        return response;
    }

    // constructor for error response
    public static <T> RpcResponse<T> error(String requestId, ResponseEnum code) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setRequestId(requestId);
        response.setStatusCode(code.getCode());
        response.setMessage(code.getMessage());
        return response;
    }
}
