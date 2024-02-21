package com.hanfei.rpc.transport.netty.codec;

import com.hanfei.rpc.entity.RpcRequest;
import com.hanfei.rpc.enums.PackageTypeEnum;
import com.hanfei.rpc.serialize.CommonSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;


public class CommonEncoder extends MessageToByteEncoder {

    private static final int MAGIC_NUMBER = 0xCAFFBABE;

    private final CommonSerializer serializer;

    // accept serializer for encoding
    public CommonEncoder(CommonSerializer serializer) {
        this.serializer = serializer;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) {
        // 1. write the magic number to the ByteBuf
        out.writeInt(MAGIC_NUMBER);

        // 2. determine the package type based on the message type
        if (msg instanceof RpcRequest) {
            out.writeInt(PackageTypeEnum.REQUEST_PACK.getCode());
        } else {
            out.writeInt(PackageTypeEnum.RESPONSE_PACK.getCode());
        }

        // 3. write the serializer code to the ByteBuf
        out.writeInt(serializer.getCode());

        // serialize the message using the selected serializer
        byte[] bytes = serializer.serialize(msg);

        // 4. write the length of the serialized data to the ByteBuf
        out.writeInt(bytes.length);

        // 5. write the serialized data to the ByteBuf
        out.writeBytes(bytes);
    }
}
