package com.hanfei.rpc.loadbalancer;

import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;

public interface LoadBalance {
    /**
     * Select one service instance from available instances
     */
    Instance select(List<Instance> instances);
}