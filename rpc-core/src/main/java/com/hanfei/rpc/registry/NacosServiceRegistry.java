package com.hanfei.rpc.registry;

import com.alibaba.nacos.api.exception.NacosException;
import com.hanfei.rpc.enums.ErrorEnum;
import com.hanfei.rpc.exception.RpcException;
import com.hanfei.rpc.util.NacosUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class NacosServiceRegistry implements ServiceRegistry {
    @Override
    public void registerService(String serviceName, InetSocketAddress serverAddress) {
        try {
            NacosUtil.registerService(serviceName, serverAddress);
        } catch (NacosException e) {
            throw new RpcException(ErrorEnum.REGISTER_SERVICE_FAILED);
        }
    }
}
