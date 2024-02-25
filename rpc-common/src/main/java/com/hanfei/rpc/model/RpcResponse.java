package com.hanfei.rpc.model;

import com.hanfei.rpc.enums.ResponseStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class RpcResponse<T> implements Serializable {
    private String requestId;
    private Integer statusCode;
    private String message;
    private T data;

    public static <T> RpcResponse<T> success(String requestId, T data) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setRequestId(requestId);
        response.setStatusCode(ResponseStatus.SUCCESS.getCode());
        response.setData(data);
        return response;
    }

    public static <T> RpcResponse<T> error(String requestId, ResponseStatus responseStatus) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setRequestId(requestId);
        response.setStatusCode(responseStatus.getCode());
        response.setMessage(responseStatus.getMsg());
        return response;
    }
}
