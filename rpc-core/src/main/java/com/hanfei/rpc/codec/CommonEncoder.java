package com.hanfei.rpc.codec;

import com.hanfei.rpc.entity.RpcRequest;
import com.hanfei.rpc.enums.PackageTypeEnum;
import com.hanfei.rpc.serializer.CommonSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 消息编码器，将数据对象编码为字节数据以便进行网络传输
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class CommonEncoder extends MessageToByteEncoder {

    /**
     * 魔数，标识一个协议包
     */
    private static final int MAGIC_NUMBER = 0xCAFEBABE;

    /**
     * 使用的序列化器
     */
    private final CommonSerializer serializer;

    /**
     * 构造函数，传入序列化器
     */
    public CommonEncoder(CommonSerializer serializer) {
        this.serializer = serializer;
    }

    /**
     * 对数据对象进行编码
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) {
        // 写入魔数
        out.writeInt(MAGIC_NUMBER);

        // 根据数据对象的类型写入数据包类型代码
        if (msg instanceof RpcRequest) {
            out.writeInt(PackageTypeEnum.REQUEST_PACK.getCode());
        } else {
            out.writeInt(PackageTypeEnum.RESPONSE_PACK.getCode());
        }

        // 写入序列化器代码
        out.writeInt(serializer.getCode());

        // 使用序列化器将数据对象序列化为字节数组
        byte[] bytes = serializer.serialize(msg);

        // 写入数据长度和实际数据
        out.writeInt(bytes.length);
        out.writeBytes(bytes);
    }
}
