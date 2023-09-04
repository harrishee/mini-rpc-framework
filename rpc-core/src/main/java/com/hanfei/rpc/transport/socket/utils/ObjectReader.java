package com.hanfei.rpc.transport.socket.utils;

import com.hanfei.rpc.entity.RpcRequest;
import com.hanfei.rpc.entity.RpcResponse;
import com.hanfei.rpc.enums.ErrorEnum;
import com.hanfei.rpc.enums.PackageTypeEnum;
import com.hanfei.rpc.exception.RpcException;
import com.hanfei.rpc.serialize.CommonSerializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;

/**
 * get object
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
@Slf4j
public class ObjectReader {

    private static final int MAGIC_NUMBER = 0xCAFFBABE;

    /**
     * get bytes from input stream, and then convert to object by deserializing
     */
    public static Object getObjectFromInStream(InputStream in) throws IOException {
        // read magic
        byte[] numberBytes = new byte[4];
        in.read(numberBytes);
        int magic = bytesToInt(numberBytes);
        if (magic != MAGIC_NUMBER) {
            log.error("Error when reading magic number: {}", magic);
            throw new RpcException(ErrorEnum.UNKNOWN_PROTOCOL);
        }

        // read package type
        in.read(numberBytes);
        int packageCode = bytesToInt(numberBytes);
        Class<?> packageClass;
        if (packageCode == PackageTypeEnum.REQUEST_PACK.getCode()) {
            packageClass = RpcRequest.class;
        } else if (packageCode == PackageTypeEnum.RESPONSE_PACK.getCode()) {
            packageClass = RpcResponse.class;
        } else {
            log.error("Error when reading package type code: {}", packageCode);
            throw new RpcException(ErrorEnum.UNKNOWN_PACKAGE_TYPE);
        }

        // read serializer type
        in.read(numberBytes);
        int serializerCode = bytesToInt(numberBytes);
        CommonSerializer serializer = CommonSerializer.getByCode(serializerCode);
        if (serializer == null) {
            log.error("Error when reading serializer code: {}", serializerCode);
            throw new RpcException(ErrorEnum.UNKNOWN_SERIALIZER);
        }

        // read data length and data bytes according to the length
        in.read(numberBytes);
        int length = bytesToInt(numberBytes);
        byte[] bytes = new byte[length];
        in.read(bytes);

        // deserialize the data bytes to an object
        return serializer.deserialize(bytes, packageClass);
    }

    public static int bytesToInt(byte[] src) {
        int value;
        value = ((src[0] & 0xFF) << 24)
                | ((src[1] & 0xFF) << 16)
                | ((src[2] & 0xFF) << 8)
                | (src[3] & 0xFF);
        return value;
    }
}
