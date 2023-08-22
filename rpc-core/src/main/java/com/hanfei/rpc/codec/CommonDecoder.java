package com.hanfei.rpc.codec;

import com.hanfei.rpc.entity.RpcRequest;
import com.hanfei.rpc.entity.RpcResponse;
import com.hanfei.rpc.enums.PackageTypeEnum;
import com.hanfei.rpc.enums.ErrorEnum;
import com.hanfei.rpc.exception.RpcException;
import com.hanfei.rpc.serializer.CommonSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 将接收到的字节数据解码成具体的 Java 对象
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class CommonDecoder extends ReplayingDecoder {

    private static final Logger logger = LoggerFactory.getLogger(CommonDecoder.class);

    /**
     * 魔数，标识一个协议包
     */
    private static final int MAGIC_NUMBER = 0xCAFEBABE;

    /**
     * 解码方法
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        // 从字节数据中读取魔数
        int magic = in.readInt();

        // 检查魔数是否匹配，若不匹配则抛出识别不到的协议包异常
        if (magic != MAGIC_NUMBER) {
            logger.error("不识别的协议包: {}", magic);
            throw new RpcException(ErrorEnum.UNKNOWN_PROTOCOL);
        }

        // 从字节数据中读取数据包类型代码
        int packageCode = in.readInt();
        Class<?> packageClass;
        // 根据数据包类型代码判断具体的数据包类型
        if (packageCode == PackageTypeEnum.REQUEST_PACK.getCode()) {
            packageClass = RpcRequest.class;
        } else if (packageCode == PackageTypeEnum.RESPONSE_PACK.getCode()) {
            packageClass = RpcResponse.class;
        } else {
            logger.error("不识别的数据包: {}", packageCode);
            throw new RpcException(ErrorEnum.UNKNOWN_PACKAGE_TYPE);
        }

        // 从字节数据中读取序列化器代码
        int serializerCode = in.readInt();
        // 根据序列化器代码获取对应的序列化器
        CommonSerializer serializer = CommonSerializer.getByCode(serializerCode);
        // 检查序列化器是否合法，若不合法则抛出未知序列化器异常
        if (serializer == null) {
            logger.error("不识别的反序列化器: {}", serializerCode);
            throw new RpcException(ErrorEnum.UNKNOWN_SERIALIZER);
        }

        // 从字节数据中读取数据长度
        int length = in.readInt();
        byte[] bytes = new byte[length];

        // 从字节数据中读取实际数据内容
        in.readBytes(bytes);
        // 使用序列化器将字节数据反序列化为指定类型的 Java 对象
        Object obj = serializer.deserialize(bytes, packageClass);
        // 将反序列化后的 Java 对象添加到输出列表中
        out.add(obj);
    }
}
