package com.hanfei.rpc.transport.socket.utils;

import com.hanfei.rpc.entity.RpcRequest;
import com.hanfei.rpc.entity.RpcResponse;
import com.hanfei.rpc.enums.ErrorEnum;
import com.hanfei.rpc.enums.PackageTypeEnum;
import com.hanfei.rpc.exception.RpcException;
import com.hanfei.rpc.serialize.CommonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * 从输入流中读取并反序列化对象
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class ObjectReader {

    private static final Logger logger = LoggerFactory.getLogger(ObjectReader.class);

    // 魔数，用于识别协议包
    private static final int MAGIC_NUMBER = 0xCAFEBABE;

    /**
     * 从输入流中读取并反序列化对象
     *
     * @param in 输入流
     * @return 反序列化得到的对象
     * @throws IOException 如果读取或反序列化过程中出现错误
     */
    public static Object readObject(InputStream in) throws IOException {
        // 读取魔数
        byte[] numberBytes = new byte[4];
        in.read(numberBytes);
        int magic = bytesToInt(numberBytes);

        // 检查魔数是否匹配
        if (magic != MAGIC_NUMBER) {
            logger.error("不识别的协议包: {}", magic);
            throw new RpcException(ErrorEnum.UNKNOWN_PROTOCOL);
        }

        // 读取数据包类型
        in.read(numberBytes);
        int packageCode = bytesToInt(numberBytes);
        Class<?> packageClass;

        // 根据数据包类型选择对应的类
        if (packageCode == PackageTypeEnum.REQUEST_PACK.getCode()) {
            packageClass = RpcRequest.class;
        } else if (packageCode == PackageTypeEnum.RESPONSE_PACK.getCode()) {
            packageClass = RpcResponse.class;
        } else {
            logger.error("不识别的数据包: {}", packageCode);
            throw new RpcException(ErrorEnum.UNKNOWN_PACKAGE_TYPE);
        }

        // 读取序列化器编码
        in.read(numberBytes);
        int serializerCode = bytesToInt(numberBytes);
        CommonSerializer serializer = CommonSerializer.getByCode(serializerCode);

        // 检查序列化器是否合法
        if (serializer == null) {
            logger.error("不识别的反序列化器: {}", serializerCode);
            throw new RpcException(ErrorEnum.UNKNOWN_SERIALIZER);
        }

        // 读取数据长度
        in.read(numberBytes);
        int length = bytesToInt(numberBytes);
        byte[] bytes = new byte[length];

        // 读取数据字节并进行反序列化
        in.read(bytes);
        return serializer.deserialize(bytes, packageClass);
    }

    /**
     * 将字节数组转换为整数
     *
     * @param src 字节数组
     * @return 转换得到的整数
     */
    public static int bytesToInt(byte[] src) {
        int value;
        value = ((src[0] & 0xFF) << 24)
                | ((src[1] & 0xFF) << 16)
                | ((src[2] & 0xFF) << 8)
                | (src[3] & 0xFF);
        return value;
    }
}
