package com.hanfei.rpc.util;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.hanfei.rpc.enums.ErrorEnum;
import com.hanfei.rpc.exception.RpcException;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * managing Nacos connections and operations
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
@Slf4j
public class NacosUtil {

    private static final NamingService namingService;

    // a set to store registered service names
    private static final Set<String> serviceNamesSet = new HashSet<>();

    private static InetSocketAddress registryAddress;

    // TODO: hard coding for now
    private static final String NACOS_SERVER_ADDR = "127.0.0.1:8848";

    static {
        namingService = getNacosNamingService();
    }

    public static NamingService getNacosNamingService() {
        try {
            return NamingFactory.createNamingService(NACOS_SERVER_ADDR);
        } catch (NacosException e) {
            log.error("Error when connecting to Nacos: {}", e.getMessage());
            throw new RpcException(ErrorEnum.FAILED_TO_CONNECT_TO_SERVICE_REGISTRY);
        }
    }

    public static void registerService(String serviceName, InetSocketAddress address) throws NacosException {
        namingService.registerInstance(serviceName, address.getHostName(), address.getPort());
        NacosUtil.registryAddress = address;
        serviceNamesSet.add(serviceName);
    }

    public static List<Instance> getAllInstance(String serviceName) throws NacosException {
        return namingService.getAllInstances(serviceName);
    }

    public static void clearRegistry() {
        if (!serviceNamesSet.isEmpty() && registryAddress != null) {
            String host = registryAddress.getHostName();
            int port = registryAddress.getPort();

            // iterate through the registered service and deregister
            Iterator<String> iterator = serviceNamesSet.iterator();
            while (iterator.hasNext()) {
                String serviceName = iterator.next();
                try {
                    namingService.deregisterInstance(serviceName, host, port);
                } catch (NacosException e) {
                    log.error("Error when deregistering service {}", e.getMessage());
                }
            }
        }
    }
}
