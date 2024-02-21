package com.hanfei.rpc.registry.nacos;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.hanfei.rpc.enums.ErrorEnum;
import com.hanfei.rpc.exception.RpcException;
import com.hanfei.rpc.loadbalance.LoadBalance;
import com.hanfei.rpc.loadbalance.RoundRobinSelect;
import com.hanfei.rpc.registry.ServiceDiscovery;
import com.hanfei.rpc.util.NacosUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;


@Slf4j
public class NacosServiceDiscovery implements ServiceDiscovery {

    private final LoadBalance loadBalance;

    public NacosServiceDiscovery(LoadBalance loadBalance) {
        if (loadBalance == null) {
            this.loadBalance = new RoundRobinSelect();
        } else {
            this.loadBalance = loadBalance;
        }
    }

    @Override
    public InetSocketAddress getServerByService(String serviceName) {
        try {
            List<Instance> instances = NacosUtil.getAllInstance(serviceName);
            if(instances.isEmpty()) {
                log.error("Cannot find corresponding service on Nacos: [{}]", serviceName);
                throw new RpcException(ErrorEnum.SERVICE_NOT_FOUND);
            }
            Instance instance = loadBalance.select(instances);
            return new InetSocketAddress(instance.getIp(), instance.getPort());
        } catch (NacosException e) {
            log.error("Error when looking up service: {}", e.getMessage());
        }
        return null;
    }
}
