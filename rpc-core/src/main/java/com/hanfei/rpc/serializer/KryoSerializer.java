package com.hanfei.rpc.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.hanfei.rpc.model.RpcRequest;
import com.hanfei.rpc.model.RpcResponse;
import com.hanfei.rpc.enums.SerializerEnum;
import com.hanfei.rpc.exception.SerializeException;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;


@Slf4j
public class KryoSerializer implements Serializer {
    // 使用 ThreadLocal 确保 Kryo 实例的线程安全性
    private static final ThreadLocal<Kryo> KRYO_THREAD_LOCAL = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.register(RpcResponse.class);
        kryo.register(RpcRequest.class);
        
        // 启用对象引用
        kryo.setReferences(true);
        // 允许未注册的类被序列化和反序列化
        kryo.setRegistrationRequired(false);
        return kryo;
    });
    
    @Override
    public byte[] serialize(Object obj) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             Output output = new Output(byteArrayOutputStream)) {
            Kryo kryo = KRYO_THREAD_LOCAL.get();
            
            // 使用 Kryo 序列化对象
            kryo.writeObject(output, obj);
            
            // 使用完毕后，从 ThreadLocal 中移除 Kryo 实例，避免内存泄漏
            KRYO_THREAD_LOCAL.remove();
            return output.toBytes();
        } catch (Exception e) {
            log.error("序列化时出现错误: {}", e.getMessage());
            throw new SerializeException("使用 Kryo 序列化时出现错误");
        }
    }
    
    @Override
    public Object deserialize(byte[] bytes, Class<?> clazz) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             Input input = new Input(byteArrayInputStream)) {
            Kryo kryo = KRYO_THREAD_LOCAL.get();
            
            // 使用 Kryo 反序列化对象
            Object res = kryo.readObject(input, clazz);
            KRYO_THREAD_LOCAL.remove();
            return res;
        } catch (Exception e) {
            log.error("反序列化时出现错误: {}", e.getMessage());
            throw new SerializeException("使用 Kryo 反序列化时出现错误");
        }
    }
    
    @Override
    public int getCode() {
        return SerializerEnum.KRYO.getCode();
    }
}
