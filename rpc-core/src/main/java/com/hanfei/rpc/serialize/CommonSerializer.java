package com.hanfei.rpc.serialize;

/**
 * 通用序列化接口，用于将对象序列化为字节数组，以及将字节数组反序列化为对象
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public interface CommonSerializer {

    Integer KRYO_SERIALIZER = 0;

    Integer JSON_SERIALIZER = 1;

    Integer DEFAULT_SERIALIZER = KRYO_SERIALIZER;

    /**
     * 序列化
     */
    byte[] serialize(Object obj);

    /**
     * 反序列化
     */
    Object deserialize(byte[] bytes, Class<?> clazz);

    /**
     * 根据编号获取对应的序列化器
     */
    static CommonSerializer getByCode(int code) {
        switch (code) {
            case 0:
                return new KryoSerializer();
            case 1:
                return new JsonSerializer();
            default:
                return null;
        }
    }

    /**
     * 获取序列化器的编号
     */
    int getCode();
}
