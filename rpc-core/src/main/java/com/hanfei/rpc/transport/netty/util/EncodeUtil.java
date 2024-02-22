package com.hanfei.rpc.transport.netty.util;

import com.hanfei.rpc.model.RpcRequest;
import com.hanfei.rpc.enums.PackageTypeEnum;
import com.hanfei.rpc.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class EncodeUtil extends MessageToByteEncoder {
    private static final int MAGIC_NUMBER = 0xCAFEBABE;
    private final Serializer serializer;

    // accept serializer for encoding
    public EncodeUtil(Serializer serializer) {
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
