package com.hanfei.rpc.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * RPC Request Object
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RpcRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    // unique identifier for the request
    private String requestId;

    // target interface name
    private String interfaceName;

    // target method name
    private String methodName;

    // method parameters
    private Object[] parameters;

    // method parameter types
    private Class<?>[] paramTypes;

    // if the request is a heartbeat
    private Boolean heartBeat;
}
