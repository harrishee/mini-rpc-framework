package com.hanfei.rpc.serializer;

/**
 * 通用序列化接口，用于将对象序列化为字节数组，以及将字节数组反序列化为对象
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public interface CommonSerializer {

    /**
     * 序列化
     */
    byte[] serialize(Object obj);

    /**
     * 反序列化
     */
    Object deserialize(byte[] bytes, Class<?> clazz);

    /**
     * 获取序列化器的编号
     */
    int getCode();

    /**
     * 根据编号获取对应的序列化器
     */
    static CommonSerializer getByCode(int code) {
        switch (code) {
            case 1:
                // 返回 JSON 序列化器
                return new JsonSerializer();
            default:
                // 未知的序列化器编号，返回 null
                return null;
        }
    }
}
