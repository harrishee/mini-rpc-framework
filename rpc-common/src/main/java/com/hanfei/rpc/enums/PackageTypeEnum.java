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
public enum PackageTypeEnum {

    REQUEST_PACK(0),

    RESPONSE_PACK(1);

    private final int code;
}
