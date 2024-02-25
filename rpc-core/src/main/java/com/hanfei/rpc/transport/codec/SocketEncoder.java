package com.hanfei.rpc.transport.codec;

import com.hanfei.rpc.model.RpcRequest;
import com.hanfei.rpc.enums.PackageTypeEnum;
import com.hanfei.rpc.serializer.Serializer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SocketEncoder {
    private static final int MAGIC_NUMBER = 0xCAFEBABE;
    
    public static void serializeAndWrite(OutputStream outputStream, Object object, Serializer serializer) throws IOException {
        // 1. 写入魔数，用于协议识别
        outputStream.write(intToBytes(MAGIC_NUMBER));
        
        // 根据对象类型写入包类型代码，区分请求包和响应包
        if (object instanceof RpcRequest) {
            outputStream.write(intToBytes(PackageTypeEnum.REQUEST.getCode()));
        } else {
            outputStream.write(intToBytes(PackageTypeEnum.RESPONSE.getCode()));
        }
        
        // 写入序列化器类型代码，指明使用的序列化器
        outputStream.write(intToBytes(serializer.getCode()));
        
        // 序列化对象获取字节数据
        byte[] bytes = serializer.serialize(object);
        
        // 写入数据长度和数据本身
        outputStream.write(intToBytes(bytes.length));
        outputStream.write(bytes);
        
        // 刷新输出流，确保数据完全写出
        outputStream.flush();
    }
    
    // 将整数值转换为4字节的字节数组
    private static byte[] intToBytes(int value) {
        byte[] src = new byte[4]; // 创建一个长度为4的字节数组
        src[0] = (byte) ((value >> 24) & 0xFF); // 最高位字节
        src[1] = (byte) ((value >> 16) & 0xFF); // 次高位字节
        src[2] = (byte) ((value >> 8) & 0xFF);  // 次低位字节
        src[3] = (byte) (value & 0xFF);         // 最低位字节
        return src; // 返回字节数组
    }
}
