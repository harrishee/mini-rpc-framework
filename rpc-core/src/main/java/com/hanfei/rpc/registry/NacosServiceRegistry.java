package com.hanfei.rpc.registry;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.hanfei.rpc.enums.ErrorEnum;
import com.hanfei.rpc.exception.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * Nacos 服务注册中心
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class NacosServiceRegistry implements ServiceRegistry {

    private static final Logger logger = LoggerFactory.getLogger(NacosServiceRegistry.class);

    // Nacos 服务注册中心默认连接地址
    private static final String SERVER_ADDR = "127.0.0.1:8848";

    // Nacos NamingService，用于与Nacos服务注册中心通信
    private static final NamingService namingService;

    // 静态代码块，在类加载时初始化NamingService
    static {
        try {
            // 创建与Nacos服务注册中心的连接
            namingService = NamingFactory.createNamingService(SERVER_ADDR);
        } catch (NacosException e) {
            logger.error("连接到 Nacos 时有错误发生: ", e);
            throw new RpcException(ErrorEnum.FAILED_TO_CONNECT_TO_SERVICE_REGISTRY);
        }
    }

    @Override
    public void register(String serviceName, InetSocketAddress inetSocketAddress) {
        try {
            // 在Nacos中注册服务实例
            namingService.registerInstance(serviceName, inetSocketAddress.getHostName(), inetSocketAddress.getPort());
        } catch (NacosException e) {
            logger.error("注册服务时有错误发生:", e);
            throw new RpcException(ErrorEnum.REGISTER_SERVICE_FAILED);
        }
    }

    @Override
    public InetSocketAddress lookupService(String serviceName) {
        try {
            // 获取特定服务的所有实例
            List<Instance> instances = namingService.getAllInstances(serviceName);
            // 先选择第一个实例，下次做负载均衡 TODO
            Instance instance = instances.get(0);
            return new InetSocketAddress(instance.getIp(), instance.getPort());
        } catch (NacosException e) {
            logger.error("获取服务时有错误发生:", e);
        }
        return null;
    }
}
