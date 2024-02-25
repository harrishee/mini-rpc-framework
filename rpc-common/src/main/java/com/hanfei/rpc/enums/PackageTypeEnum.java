package com.hanfei.rpc.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PackageTypeEnum {
    REQUEST(0),
    RESPONSE(1);

    private final int code;
}
