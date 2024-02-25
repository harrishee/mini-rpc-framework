package com.hanfei.rpc.serializer;

public interface Serializer {
    Integer KRYO_SERIALIZER = 0;
    Integer JSON_SERIALIZER = 1;
    Integer DEFAULT_SERIALIZER = KRYO_SERIALIZER;

    byte[] serialize(Object obj);

    Object deserialize(byte[] bytes, Class<?> clazz);

    int getCode();

    static Serializer getSerializer(int code) {
        switch (code) {
            case 1:
                return new JsonSerializer();
            case 0:
            default:
                return new KryoSerializer();
        }
    }
}
