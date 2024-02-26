package com.hanfei.rpc.util;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.hanfei.rpc.enums.ErrorEnum;
import com.hanfei.rpc.exception.RpcException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NacosUtil {
    private static final String NACOS_SERVER_ADDR = System.getenv().getOrDefault("NACOS_SERVER_ADDR", "127.0.0.1:8848");
    private static InetSocketAddress registryAddr;
    private static final Set<String> REGISTERED_SERVICE = new HashSet<>();
    private static final NamingService NAMING_SERVICE = initNamingService();
    
    private static NamingService initNamingService() {
        try {
            return NamingFactory.createNamingService(NACOS_SERVER_ADDR);
        } catch (NacosException e) {
            log.error("连接Nacons注册中心时出错: ", e);
            throw new RpcException(ErrorEnum.FAILED_TO_CONNECT_TO_SERVICE_REGISTRY);
        }
    }
    
    public static void registerService(String serviceName, InetSocketAddress serverAddress) throws NacosException {
        registryAddr = serverAddress;
        REGISTERED_SERVICE.add(serviceName);
        NAMING_SERVICE.registerInstance(serviceName, serverAddress.getHostName(), serverAddress.getPort());
        log.info("Nacos服务注册成功: [{} -> {}]", serviceName, serverAddress);
    }
    
    public static List<Instance> getAllInstance(String serviceName) throws NacosException {
        return NAMING_SERVICE.getAllInstances(serviceName);
    }
    
    public static void clearRegistry() {
        if (!REGISTERED_SERVICE.isEmpty() && registryAddr != null) {
            String host = registryAddr.getHostName();
            int port = registryAddr.getPort();
            REGISTERED_SERVICE.forEach(serviceName -> {
                try {
                    NAMING_SERVICE.deregisterInstance(serviceName, host, port);
                    log.info("Nacons服务注销成功: [{} -> {}]", serviceName, registryAddr);
                } catch (NacosException e) {
                    log.error("Nacons服务注销失败: [{} -> {}]", serviceName, registryAddr);
                }
            });
        }
    }
}
