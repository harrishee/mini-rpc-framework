package com.hanfei.rpc.entity;

import com.hanfei.rpc.enums.ResponseEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
@Data
public class RpcResponse<T> implements Serializable {

    public RpcResponse() {
    }

    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;

    /**
     * 响应状态码
     */
    private Integer statusCode;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 创建一个成功的响应，包含状态码、消息和数据
     */
    public static <T> RpcResponse<T> success(T data) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setStatusCode(ResponseEnum.SUCCESS.getCode());
        response.setMessage(ResponseEnum.SUCCESS.getMessage());
        response.setData(data);
        return response;
    }

    /**
     * 创建一个失败的响应，包含状态码和消息
     */
    public static <T> RpcResponse<T> error(ResponseEnum code) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setStatusCode(code.getCode());
        response.setMessage(code.getMessage());
        return response;
    }
}
