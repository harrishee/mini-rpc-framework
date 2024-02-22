package com.hanfei.rpc.transport.netty.util;

import com.hanfei.rpc.model.RpcRequest;
import com.hanfei.rpc.model.RpcResponse;
import com.hanfei.rpc.enums.ErrorEnum;
import com.hanfei.rpc.enums.PackageTypeEnum;
import com.hanfei.rpc.exception.RpcException;
import com.hanfei.rpc.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class DecodeUtil extends ReplayingDecoder {
    private static final int MAGIC_NUMBER = 0xCAFEBABE;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        // 1. read the magic number from the incoming ByteBuf
        int magic = in.readInt();
        if (magic != MAGIC_NUMBER) {
            log.error("Error when decoding magic number: {}", magic);
            throw new RpcException(ErrorEnum.UNKNOWN_PROTOCOL);
        }

        // 2. read the package code from the incoming ByteBuf
        int packageCode = in.readInt();
        Class<?> packageClass;
        if (packageCode == PackageTypeEnum.REQUEST_PACK.getCode()) {
            packageClass = RpcRequest.class;
        } else if (packageCode == PackageTypeEnum.RESPONSE_PACK.getCode()) {
            packageClass = RpcResponse.class;
        } else {
            log.error("Error when decoding package type code: {}", packageCode);
            throw new RpcException(ErrorEnum.UNKNOWN_PACKAGE_TYPE);
        }

        // 3. read the serializer code from the incoming ByteBuf
        int serializerCode = in.readInt();
        Serializer serializer = Serializer.getByCode(serializerCode);
        if (serializer == null) {
            log.error("Error when decoding serializer code: {}", serializerCode);
            throw new RpcException(ErrorEnum.UNKNOWN_SERIALIZER);
        }

        // 4/5. read the length of the serialized data from the incoming ByteBuf
        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readBytes(bytes);

        // deserialize the data using the selected serializer and class
        Object obj = serializer.deserialize(bytes, packageClass);
        out.add(obj);
    }
}
