package com.hanfei.rpc.transport.socket.utils;

import com.hanfei.rpc.entity.RpcRequest;
import com.hanfei.rpc.enums.PackageTypeEnum;
import com.hanfei.rpc.serialize.CommonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 将对象序列化并写入输出流
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class ObjectWriter {

    private static final Logger logger = LoggerFactory.getLogger(ObjectWriter.class);

    // 魔数，用于识别协议包
    private static final int MAGIC_NUMBER = 0xCAFEBABE;

    /**
     * 将对象序列化并写入输出流
     *
     * @param outputStream 输出流
     * @param object       要写入的对象
     * @param serializer   序列化器
     * @throws IOException 如果写入过程中出现错误
     */
    public static void writeObject(OutputStream outputStream, Object object, CommonSerializer serializer)
            throws IOException {

        // 写入魔数
        outputStream.write(intToBytes(MAGIC_NUMBER));

        // 根据对象类型写入数据包类型
        if (object instanceof RpcRequest) {
            outputStream.write(intToBytes(PackageTypeEnum.REQUEST_PACK.getCode()));
        } else {
            outputStream.write(intToBytes(PackageTypeEnum.RESPONSE_PACK.getCode()));
        }

        // 写入序列化器编码
        outputStream.write(intToBytes(serializer.getCode()));

        // 获取对象的字节数组
        byte[] bytes = serializer.serialize(object);

        // 写入数据长度和数据字节
        outputStream.write(intToBytes(bytes.length));
        outputStream.write(bytes);
        outputStream.flush();
        logger.info("对象已成功序列化并写入输出流.");
    }


    /**
     * 将整数转换为字节数组
     *
     * @param value 要转换的整数
     * @return 转换得到的字节数组
     */
    private static byte[] intToBytes(int value) {
        byte[] src = new byte[4];
        src[0] = (byte) ((value >> 24) & 0xFF);
        src[1] = (byte) ((value >> 16) & 0xFF);
        src[2] = (byte) ((value >> 8) & 0xFF);
        src[3] = (byte) (value & 0xFF);
        return src;
    }
}
