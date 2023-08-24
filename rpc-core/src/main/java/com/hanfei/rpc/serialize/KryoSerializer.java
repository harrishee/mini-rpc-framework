package com.hanfei.rpc.serialize;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.hanfei.rpc.entity.RpcRequest;
import com.hanfei.rpc.entity.RpcResponse;
import com.hanfei.rpc.enums.SerializerEnum;
import com.hanfei.rpc.exception.SerializeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Kryo 序列化器，将对象序列化为字节数组，或将字节数组反序列化为对象
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class KryoSerializer implements CommonSerializer {

    private static final Logger logger = LoggerFactory.getLogger(KryoSerializer.class);

    // 使用 ThreadLocal 来保证线程安全的 Kryo 实例
    private static final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.register(RpcResponse.class);
        kryo.register(RpcRequest.class);

        kryo.setReferences(true);
        kryo.setRegistrationRequired(false);
        return kryo;
    });

    @Override
    public byte[] serialize(Object obj) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             Output output = new Output(byteArrayOutputStream)) {
            // 获取当前线程的 Kryo 实例
            Kryo kryo = kryoThreadLocal.get();

            // 使用 Kryo 将对象序列化到输出流中
            kryo.writeObject(output, obj);

            // 移除当前线程的 Kryo 实例，避免内存泄漏
            kryoThreadLocal.remove();

            // 将输出流中的序列化数据转换为字节数组并返回
            return output.toBytes();
        } catch (Exception e) {
            logger.error("序列化时发生错误:", e);
            throw new SerializeException("序列化时发生错误");
        }
    }

    @Override
    public Object deserialize(byte[] bytes, Class<?> clazz) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             Input input = new Input(byteArrayInputStream)) {
            Kryo kryo = kryoThreadLocal.get();

            // 使用 Kryo 从输入流中反序列化对象，并指定反序列化的目标类
            Object obj = kryo.readObject(input, clazz);

            kryoThreadLocal.remove();
            return obj;
        } catch (Exception e) {
            logger.error("反序列化时发生错误:", e);
            throw new SerializeException("反序列化时发生错误");
        }
    }

    @Override
    public int getCode() {
        return SerializerEnum.valueOf("KRYO").getCode();
    }
}
