package com.hanfei.rpc.loadbalancer;

import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;

/**
 * 负载均衡接口
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public interface LoadBalancer {

    /**
     * 从实例列表中选择下一个要调用的实例
     */
    Instance select(List<Instance> instances);
}
