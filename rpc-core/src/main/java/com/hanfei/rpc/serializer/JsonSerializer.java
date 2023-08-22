package com.hanfei.rpc.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanfei.rpc.entity.RpcRequest;
import com.hanfei.rpc.enums.SerializerEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * JSON 序列化器，将对象序列化为 JSON 字符串，或将 JSON 字符串反序列化为对象
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class JsonSerializer implements CommonSerializer {

    private static final Logger logger = LoggerFactory.getLogger(JsonSerializer.class);

    /**
     * ObjectMapper 对象用于实现 JSON 数据与 Java 对象之间的相互转换
     */
    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 将对象序列化为 JSON 字节数组
     */
    @Override
    public byte[] serialize(Object obj) {
        try {
            // 将对象序列化为 JSON 字符串
            return objectMapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            logger.error("序列化时有错误发生: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 将 JSON 字节数组反序列化为指定类型的对象
     */
    @Override
    public Object deserialize(byte[] bytes, Class<?> clazz) {
        try {
            // 将 JSON 字节数组反序列化为对象
            Object obj = objectMapper.readValue(bytes, clazz);

            // 如果反序列化的对象是 请求 类型，则需要处理参数类型匹配的问题
            if (obj instanceof RpcRequest) {
                obj = handleRequest(obj);
            }
            return obj;
        } catch (IOException e) {
            logger.error("反序列化时有错误发生: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 处理请求对象的参数类型匹配
     */
    private Object handleRequest(Object obj) throws IOException {
        // 将传入的对象强制转换为 请求 类型
        RpcRequest rpcRequest = (RpcRequest) obj;

        // 遍历 请求对象 中的参数类型和参数值数组
        for (int i = 0; i < rpcRequest.getParamTypes().length; i++) {
            // 获取参数类型
            Class<?> clazz = rpcRequest.getParamTypes()[i];

            // 检查参数值是否可以赋值给参数类型，若不可以则需要重新反序列化
            if (!clazz.isAssignableFrom(rpcRequest.getParameters()[i].getClass())) {
                // 将参数值序列化为字节数组
                byte[] bytes = objectMapper.writeValueAsBytes(rpcRequest.getParameters()[i]);

                // 将字节数组反序列化为指定类型的对象，并替换原始参数值
                rpcRequest.getParameters()[i] = objectMapper.readValue(bytes, clazz);
            }
        }
        // 返回经过处理的 请求对象
        return rpcRequest;
    }

    /**
     * 获取序列化器的编号，这里返回 JSON 序列化器的编号
     */
    @Override
    public int getCode() {
        return SerializerEnum.valueOf("JSON").getCode();
    }
}
