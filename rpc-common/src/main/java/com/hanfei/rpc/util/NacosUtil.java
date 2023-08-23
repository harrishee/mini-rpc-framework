package com.hanfei.rpc.util;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.hanfei.rpc.enums.ErrorEnum;
import com.hanfei.rpc.exception.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * 管理 nacos 连接等工具类
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class NacosUtil {

    private static final Logger logger = LoggerFactory.getLogger(NacosUtil.class);

    // Nacos 服务发现客户端
    private static final NamingService namingService;

    // 用于存储已注册的服务名集合
    private static final Set<String> serviceNamesSet = new HashSet<>();

    // 保存注册中心地址
    private static InetSocketAddress address;

    // Nacos 服务器地址
    private static final String SERVER_ADDR = "127.0.0.1:8848";

    static {
        // 初始化 Nacos 服务发现客户端
        namingService = getNacosNamingService();
    }

    /**
     * 获取 Nacos 服务发现客户端
     */
    public static NamingService getNacosNamingService() {
        try {
            // 使用指定的 Nacos 服务器地址创建 NamingService 实例
            return NamingFactory.createNamingService(SERVER_ADDR);
        } catch (NacosException e) {
            logger.error("连接到Nacos时有错误发生: ", e);
            throw new RpcException(ErrorEnum.FAILED_TO_CONNECT_TO_SERVICE_REGISTRY);
        }
    }

    /**
     * 注册服务到 Nacos 注册中心
     */
    public static void registerService(String serviceName, InetSocketAddress address) throws NacosException {
        // 向 Nacos 注册中心注册服务
        namingService.registerInstance(serviceName, address.getHostName(), address.getPort());
        // 保存注册中心地址
        NacosUtil.address = address;
        // 保存已注册的服务名
        serviceNamesSet.add(serviceName);

    }

    /**
     * 获取指定服务名的所有实例
     */
    public static List<Instance> getAllInstance(String serviceName) throws NacosException {
        return namingService.getAllInstances(serviceName);
    }

    /**
     * 清空注册信息
     */
    public static void clearRegistry() {
        // 检查服务名集合是否为空，且注册中心地址是否不为 null
        if (!serviceNamesSet.isEmpty() && address != null) {
            // 获取注册中心主机名和端口号
            String host = address.getHostName();
            int port = address.getPort();

            // 遍历服务名集合，逐个注销服务
            Iterator<String> iterator = serviceNamesSet.iterator();
            while (iterator.hasNext()) {
                String serviceName = iterator.next();
                try {
                    // 从 Nacos 注册中心注销当前服务名的实例
                    namingService.deregisterInstance(serviceName, host, port);
                } catch (NacosException e) {
                    logger.error("注销服务 {} 失败", serviceName, e);
                }
            }
        }
    }
}
