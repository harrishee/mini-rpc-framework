package com.hanfei.rpc.serialize;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanfei.rpc.entity.RpcRequest;
import com.hanfei.rpc.enums.SerializerEnum;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;


@Slf4j
public class JsonSerializer implements CommonSerializer {

    // ObjectMapper used to implement the mutual conversion between JSON data and Java object
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] serialize(Object obj) {
        try {
            return objectMapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            log.error("Error when serializing: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public Object deserialize(byte[] bytes, Class<?> clazz) {
        try {
            Object obj = objectMapper.readValue(bytes, clazz);

            // if the object is a request object, then we need to handle the parameter type matching
            if (obj instanceof RpcRequest) {
                obj = handleRequest(obj);
            }
            return obj;
        } catch (IOException e) {
            log.error("Error when deserializing: {}", e.getMessage());
            return null;
        }
    }

    /**
     * handle the parameter type matching
     */
    private Object handleRequest(Object obj) throws IOException {
        RpcRequest rpcRequest = (RpcRequest) obj;

        for (int i = 0; i < rpcRequest.getParamTypes().length; i++) {
            // get the parameter type
            Class<?> clazz = rpcRequest.getParamTypes()[i];

            // check whether the parameter value can be assigned to the parameter type
            // if not, then we need to deserialize it again
            if (!clazz.isAssignableFrom(rpcRequest.getParameters()[i].getClass())) {
                byte[] bytes = objectMapper.writeValueAsBytes(rpcRequest.getParameters()[i]);
                rpcRequest.getParameters()[i] = objectMapper.readValue(bytes, clazz);
            }
        }
        return rpcRequest;
    }

    @Override
    public int getCode() {
        return SerializerEnum.valueOf("JSON").getCode();
    }
}
