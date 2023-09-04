package com.hanfei.rpc.loadbalance;

import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;

/**
 * Round-robin strategy
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class RoundRobinSelect implements LoadBalance {

    private int index = 0;

    @Override
    public Instance select(List<Instance> instances) {
        // if the index is out of bound, reset it to 0
        if (index >= instances.size()) {
            index %= instances.size();
        }
        return instances.get(index++);
    }
}
