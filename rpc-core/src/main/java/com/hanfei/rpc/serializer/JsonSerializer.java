package com.hanfei.rpc.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanfei.rpc.model.RpcRequest;
import com.hanfei.rpc.enums.SerializerEnum;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;


@Slf4j
public class JsonSerializer implements Serializer {
    // ObjectMapper 用于实现 JSON 数据与 Java 对象之间的相互转换
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public byte[] serialize(Object obj) {
        try {
            // 将对象序列化为 JSON 字节数组
            return objectMapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            log.error("序列化时出现错误: {}", e.getMessage());
            return new byte[0];
        }
    }
    
    @Override
    public Object deserialize(byte[] bytes, Class<?> clazz) {
        try {
            // 将 JSON 字节数组反序列化为对象
            Object res = objectMapper.readValue(bytes, clazz);
            // 如果是 RpcRequest 对象，则需要处理参数类型匹配
            if (res instanceof RpcRequest) {
                res = handleRequest(res);
            }
            return res;
        } catch (IOException e) {
            log.error("反序列化时出现错误: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 处理参数类型匹配问题
     */
    private Object handleRequest(Object obj) throws IOException {
        RpcRequest rpcRequest = (RpcRequest) obj;
        
        for (int i = 0; i < rpcRequest.getParamTypes().length; i++) {
            Class<?> clazz = rpcRequest.getParamTypes()[i];
            // 检查参数值是否可以赋值给参数类型，如果不行，则需要再次反序列化
            if (!clazz.isAssignableFrom(rpcRequest.getParameters()[i].getClass())) {
                byte[] bytes = objectMapper.writeValueAsBytes(rpcRequest.getParameters()[i]);
                rpcRequest.getParameters()[i] = objectMapper.readValue(bytes, clazz);
            }
        }
        return rpcRequest;
    }
    
    @Override
    public int getCode() {
        return SerializerEnum.JSON.getCode();
    }
}
