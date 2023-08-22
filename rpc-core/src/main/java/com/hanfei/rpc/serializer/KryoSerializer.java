package com.hanfei.rpc.serializer;

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
        // 注册需要序列化和反序列化的类
        kryo.register(RpcResponse.class);
        kryo.register(RpcRequest.class);

        // 在序列化和反序列化时是否处理对象之间的引用关系
        // true 表示相同的对象在序列化后会被保存为引用，而不是重复序列化。这可以减小序列化数据的大小，但需要更多的处理开销
        kryo.setReferences(true);
        // 在序列化和反序列化时必须提前注册所有需要序列化的类
        // false 表示允许在序列化和反序列化时动态地注册类，如果遇到未注册的类，它会自动注册并继续处理
        kryo.setRegistrationRequired(false);
        return kryo;
    });

    /**
     * 将对象序列化为字节数组
     */
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
            logger.error("序列化时有错误发生:", e);
            throw new SerializeException("序列化时有错误发生");
        }
    }

    /**
     * 将字节数组反序列化为指定类型的对象
     */
    @Override
    public Object deserialize(byte[] bytes, Class<?> clazz) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             Input input = new Input(byteArrayInputStream)) {
            // 获取当前线程的 Kryo 实例
            Kryo kryo = kryoThreadLocal.get();

            // 使用 Kryo 从输入流中反序列化对象，并指定反序列化的目标类
            Object targetObj = kryo.readObject(input, clazz);

            // 移除当前线程的 Kryo 实例，避免内存泄漏
            kryoThreadLocal.remove();

            // 返回反序列化后的对象
            return targetObj;
        } catch (Exception e) {
            logger.error("反序列化时有错误发生:", e);
            throw new SerializeException("反序列化时有错误发生");
        }
    }

    /**
     * 获取序列化器的编号
     */
    @Override
    public int getCode() {
        return SerializerEnum.valueOf("KRYO").getCode();
    }
}
