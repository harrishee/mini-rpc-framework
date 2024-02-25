package com.hanfei.rpc.transport.codec;

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
public class NettyDecoder extends ReplayingDecoder<Void> {
    private static final int MAGIC_NUMBER = 0xCAFEBABE;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        // 自定义协议格式：4字节魔数 + 4字节包类型 + 4字节序列化器类型 + 4字节数据长度 + 数据（数据长度）

        // 1. 读取4字节魔数并进行校验
        int magic = in.readInt();
        if (magic != MAGIC_NUMBER) {
            log.error("不识别的协议包: {}", magic);
            throw new RpcException(ErrorEnum.UNKNOWN_PROTOCOL);
        }

        // 2. 读取4字节包类型，确定是请求包还是响应包
        int packageType = in.readInt();
        Class<?> packageClass;
        if (packageType == PackageTypeEnum.REQUEST.getCode()) {
            packageClass = RpcRequest.class;
        } else if (packageType == PackageTypeEnum.RESPONSE.getCode()) {
            packageClass = RpcResponse.class;
        } else {
            log.error("不识别的数据包类型: {}", packageType);
            throw new RpcException(ErrorEnum.UNKNOWN_PACKAGE_TYPE);
        }

        // 3. 读取4字节序列化器类型，确定使用哪种序列化器
        int serializerCode = in.readInt();
        Serializer serializer = Serializer.getSerializer(serializerCode);
        if (serializer == null) {
            log.error("不识别的序列化器: {}", serializerCode);
            throw new RpcException(ErrorEnum.UNKNOWN_SERIALIZER);
        }

        // 4. 读取4字节数据长度，根据长度读取数据
        int dateLength = in.readInt();

        // 5. 读取数据
        byte[] dataByte = new byte[dateLength];
        in.readBytes(dataByte);

        // 使用序列化器将字节数组反序列化为对象，并添加到解码列表中
        Object data = serializer.deserialize(dataByte, packageClass);
        out.add(data);
    }
}
