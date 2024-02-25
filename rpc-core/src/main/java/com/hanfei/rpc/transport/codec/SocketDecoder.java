package com.hanfei.rpc.transport.codec;

import com.hanfei.rpc.model.RpcRequest;
import com.hanfei.rpc.model.RpcResponse;
import com.hanfei.rpc.enums.ErrorEnum;
import com.hanfei.rpc.enums.PackageTypeEnum;
import com.hanfei.rpc.exception.RpcException;
import com.hanfei.rpc.serializer.Serializer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SocketDecoder {
    private static final int MAGIC_NUMBER = 0xCAFEBABE;
    
    public static Object readAndDeserialize(InputStream in) throws IOException {
        // 读取魔数，验证是否为RPC协议包
        byte[] numberBytes = new byte[4];
        in.read(numberBytes);
        int magic = bytesToInt(numberBytes);
        if (magic != MAGIC_NUMBER) {
            log.error("读取魔数错误: {}", magic);
            throw new RpcException(ErrorEnum.UNKNOWN_PROTOCOL);
        }
        
        // 读取包类型，确定是请求包还是响应包
        in.read(numberBytes);
        int packageCode = bytesToInt(numberBytes);
        Class<?> packageClass;
        if (packageCode == PackageTypeEnum.REQUEST.getCode()) {
            packageClass = RpcRequest.class;
        } else if (packageCode == PackageTypeEnum.RESPONSE.getCode()) {
            packageClass = RpcResponse.class;
        } else {
            log.error("读取包类型码错误: {}", packageCode);
            throw new RpcException(ErrorEnum.UNKNOWN_PACKAGE_TYPE);
        }
        
        // 读取序列化器类型，确定使用哪种序列化器
        in.read(numberBytes);
        int serializerCode = bytesToInt(numberBytes);
        Serializer serializer = Serializer.getSerializer(serializerCode);
        if (serializer == null) {
            log.error("读取序列化器码错误: {}", serializerCode);
            throw new RpcException(ErrorEnum.UNKNOWN_SERIALIZER);
        }
        
        // 读取数据长度，然后根据长度读取数据
        in.read(numberBytes);
        int length = bytesToInt(numberBytes);
        byte[] bytes = new byte[length];
        in.read(bytes);
        
        // 将读取的二进制数据通过序列化器反序列化成对象
        return serializer.deserialize(bytes, packageClass);
    }
    
    // 将4字节的字节数组转换为整数
    public static int bytesToInt(byte[] src) {
        int value;
        value = ((src[0] & 0xFF) << 24) // 最高位
                | ((src[1] & 0xFF) << 16) // 次高位
                | ((src[2] & 0xFF) << 8)  // 次低位
                | (src[3] & 0xFF);        // 最低位
        return value;
    }
}
