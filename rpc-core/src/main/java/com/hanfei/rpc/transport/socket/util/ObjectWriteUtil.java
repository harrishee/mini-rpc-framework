package com.hanfei.rpc.transport.socket.util;

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
public class ObjectWriteUtil {
    private static final int MAGIC_NUMBER = 0xCAFEBABE;

    /**
     * serialize the object to data byte, then write it to out stream
     */
    public static void writeObject(OutputStream outputStream, Object object, Serializer serializer) throws IOException {
        // write magic
        outputStream.write(intToBytes(MAGIC_NUMBER));

        // write package type
        if (object instanceof RpcRequest) {
            outputStream.write(intToBytes(PackageTypeEnum.REQUEST_PACK.getCode()));
        } else {
            outputStream.write(intToBytes(PackageTypeEnum.RESPONSE_PACK.getCode()));
        }

        // write serializer type
        outputStream.write(intToBytes(serializer.getCode()));

        // get data bytes by serializing
        byte[] bytes = serializer.serialize(object);

        // write data length and data
        outputStream.write(intToBytes(bytes.length));
        outputStream.write(bytes);
        outputStream.flush();
    }

    private static byte[] intToBytes(int value) {
        byte[] src = new byte[4];
        src[0] = (byte) ((value >> 24) & 0xFF);
        src[1] = (byte) ((value >> 16) & 0xFF);
        src[2] = (byte) ((value >> 8) & 0xFF);
        src[3] = (byte) (value & 0xFF);
        return src;
    }
}
