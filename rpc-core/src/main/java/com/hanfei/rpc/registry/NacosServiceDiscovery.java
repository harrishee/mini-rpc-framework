package com.hanfei.rpc.registry;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.hanfei.rpc.enums.ErrorEnum;
import com.hanfei.rpc.exception.RpcException;
import com.hanfei.rpc.loadbalancer.LoadBalancer;
import com.hanfei.rpc.loadbalancer.RoundRobin;
import com.hanfei.rpc.util.NacosUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;

@Slf4j
public class NacosServiceDiscovery implements ServiceDiscovery {
    private final LoadBalancer loadBalancer;
    
    public NacosServiceDiscovery(LoadBalancer loadBalancer) {
        this.loadBalancer = (loadBalancer != null) ? loadBalancer : new RoundRobin();
    }
    
    @Override
    public InetSocketAddress discoverService(String serviceName) {
        try {
            List<Instance> instances = NacosUtil.getAllInstance(serviceName);
            if (instances.isEmpty()) {
                log.error("在Nacos中找不到相应的服务实例: [{}]", serviceName);
                throw new RpcException(ErrorEnum.SERVICE_NOT_FOUND);
            }
            Instance instance = loadBalancer.select(instances);
            return new InetSocketAddress(instance.getIp(), instance.getPort());
        } catch (NacosException e) {
            log.error("Nacos查找服务时出错: ", e);
        }
        
        return null;
    }
}
