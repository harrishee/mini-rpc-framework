package com.hanfei.rpc.registry;

import com.alibaba.nacos.api.exception.NacosException;
import com.hanfei.rpc.enums.ErrorEnum;
import com.hanfei.rpc.exception.RpcException;
import com.hanfei.rpc.util.NacosUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * Nacos 服务注册中心
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class NacosServiceRegistry implements ServiceRegistry {

    private static final Logger logger = LoggerFactory.getLogger(NacosServiceRegistry.class);

    @Override
    public void register(String serviceName, InetSocketAddress inetSocketAddress) {
        try {
            // 在 Nacos 服务注册中心注册服务实例
            NacosUtil.registerService(serviceName, inetSocketAddress);
        } catch (NacosException e) {
            logger.error("注册服务时有错误发生: ", e);
            throw new RpcException(ErrorEnum.REGISTER_SERVICE_FAILED);
        }
    }
}
