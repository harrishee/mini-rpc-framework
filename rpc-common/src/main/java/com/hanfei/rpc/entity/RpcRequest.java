package com.hanfei.rpc.entity;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
@Data
@Builder
public class RpcRequest implements Serializable {

    private String interfaceName;

    private String methodName;

    private Object[] parameters;

    private Class<?>[] paramTypes;
}
