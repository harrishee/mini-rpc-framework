package com.hanfei.rpc.transport.netty.codec;

import com.hanfei.rpc.entity.RpcRequest;
import com.hanfei.rpc.enums.PackageTypeEnum;
import com.hanfei.rpc.serialize.CommonSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 消息编码器
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class CommonEncoder extends MessageToByteEncoder {

    private static final int MAGIC_NUMBER = 0xCAFEBABE;

    private final CommonSerializer serializer;

    public CommonEncoder(CommonSerializer serializer) {
        this.serializer = serializer;
    }

    /**
     * 对数据对象进行编码
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) {
        out.writeInt(MAGIC_NUMBER);

        if (msg instanceof RpcRequest) {
            out.writeInt(PackageTypeEnum.REQUEST_PACK.getCode());
        } else {
            out.writeInt(PackageTypeEnum.RESPONSE_PACK.getCode());
        }
        out.writeInt(serializer.getCode());

        byte[] bytes = serializer.serialize(msg);
        out.writeInt(bytes.length);
        out.writeBytes(bytes);
    }
}
