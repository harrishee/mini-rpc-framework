package com.hanfei.rpc.loadbalance;

import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;

/**
 * Load balancing strategy interface
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public interface LoadBalance {

    /**
     * Select one service instance from available instances
     */
    Instance select(List<Instance> instances);
}
