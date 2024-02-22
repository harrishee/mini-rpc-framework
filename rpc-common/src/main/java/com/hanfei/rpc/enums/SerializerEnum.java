package com.hanfei.rpc.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SerializerEnum {
    KRYO(0),
    JSON(1);

    private final int code;
}
