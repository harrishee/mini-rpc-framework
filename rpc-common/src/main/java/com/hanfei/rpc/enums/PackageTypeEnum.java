package com.hanfei.rpc.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PackageTypeEnum {
    REQUEST_PACK(0),
    RESPONSE_PACK(1);

    private final int code;
}
