package com.hanfei.rpc.registry.nacos;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.hanfei.rpc.enums.ErrorEnum;
import com.hanfei.rpc.exception.RpcException;
import com.hanfei.rpc.loadbalance.LoadBalance;
import com.hanfei.rpc.loadbalance.RandomLoadBalance;
import com.hanfei.rpc.registry.ServiceDiscovery;
import com.hanfei.rpc.util.NacosUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class NacosServiceDiscovery implements ServiceDiscovery {
    private static final Logger logger = LoggerFactory.getLogger(NacosServiceDiscovery.class);

    private final LoadBalance loadBalance;

    public NacosServiceDiscovery(LoadBalance loadBalance) {
        if (loadBalance == null) {
            this.loadBalance = new RandomLoadBalance();
        } else {
            this.loadBalance = loadBalance;
        }
    }

    @Override
    public InetSocketAddress lookupService(String serviceName) {
        try {
            List<Instance> instances = NacosUtil.getAllInstance(serviceName);
            if(instances.isEmpty()) {
                logger.error("找不到对应的服务: " + serviceName);
                throw new RpcException(ErrorEnum.SERVICE_NOT_FOUND);
            }
            Instance instance = loadBalance.select(instances);
            return new InetSocketAddress(instance.getIp(), instance.getPort());
        } catch (NacosException e) {
            logger.error("获取服务时发生错误: ", e);
        }
        return null;
    }
}
