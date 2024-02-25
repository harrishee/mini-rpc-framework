package com.hanfei.rpc.transport.codec;

import com.hanfei.rpc.model.RpcRequest;
import com.hanfei.rpc.enums.PackageTypeEnum;
import com.hanfei.rpc.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class NettyEncoder extends MessageToByteEncoder<Object> {
    private static final int MAGIC_NUMBER = 0xCAFEBABE;
    private final Serializer serializer;

    public NettyEncoder(Serializer serializer) {
        this.serializer = serializer;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) {
        // 自定义协议格式：4字节魔数 + 4字节包类型 + 4字节序列化器类型 + 4字节数据长度 + 数据（数据长度）

        // 1. 写入魔数，用于标识一个自定义协议包
        out.writeInt(MAGIC_NUMBER);

        // 2. 写入包类型代码，区分请求包和响应包
        if (msg instanceof RpcRequest) {
            out.writeInt(PackageTypeEnum.REQUEST.getCode());
        } else {
            out.writeInt(PackageTypeEnum.RESPONSE.getCode());
        }

        // 3. 写入序列化器类型，指明使用的序列化器
        out.writeInt(serializer.getCode());

        // 使用序列化器将对象序列化为字节数组
        byte[] serializedData = serializer.serialize(msg);

        // 4. 写入数据长度，即数据部分的字节数组长度
        out.writeInt(serializedData.length);

        // 5. 写入数据，即序列化后的字节数组
        out.writeBytes(serializedData);
    }
}
