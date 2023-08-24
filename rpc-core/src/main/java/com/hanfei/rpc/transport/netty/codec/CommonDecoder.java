package com.hanfei.rpc.transport.netty.codec;

import com.hanfei.rpc.entity.RpcRequest;
import com.hanfei.rpc.entity.RpcResponse;
import com.hanfei.rpc.enums.PackageTypeEnum;
import com.hanfei.rpc.enums.ErrorEnum;
import com.hanfei.rpc.exception.RpcException;
import com.hanfei.rpc.serialize.CommonSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 消息解码器
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class CommonDecoder extends ReplayingDecoder {

    private static final Logger logger = LoggerFactory.getLogger(CommonDecoder.class);

    private static final int MAGIC_NUMBER = 0xCAFEBABE;

    public static void main(String[] args) {
        System.out.println(MAGIC_NUMBER);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        int magic = in.readInt();

        if (magic != MAGIC_NUMBER) {
            logger.error("MAGIC NUMBER ERROR: {}", magic);
            throw new RpcException(ErrorEnum.UNKNOWN_PROTOCOL);
        }

        // 从字节数据中读取数据包类型代码
        int packageCode = in.readInt();
        Class<?> packageClass;
        if (packageCode == PackageTypeEnum.REQUEST_PACK.getCode()) {
            packageClass = RpcRequest.class;
        } else if (packageCode == PackageTypeEnum.RESPONSE_PACK.getCode()) {
            packageClass = RpcResponse.class;
        } else {
            logger.error("PACKAGE CODE ERROR: {}", packageCode);
            throw new RpcException(ErrorEnum.UNKNOWN_PACKAGE_TYPE);
        }

        // 从字节数据中读取序列化器代码
        int serializerCode = in.readInt();
        CommonSerializer serializer = CommonSerializer.getByCode(serializerCode);
        if (serializer == null) {
            logger.error("SERIALIZER CODE ERROR: {}", serializerCode);
            throw new RpcException(ErrorEnum.UNKNOWN_SERIALIZER);
        }

        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readBytes(bytes);
        Object obj = serializer.deserialize(bytes, packageClass);
        // 将反序列化后的 Java 对象添加到输出列表中
        out.add(obj);
    }
}
