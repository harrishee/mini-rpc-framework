package com.hanfei.rpc.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
@Getter
@AllArgsConstructor
public enum SerializerEnum {

    JSON(1);

    private final int code;
}
