package com.hanfei.rpc.loadbalance;

import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;
import java.util.Random;

/**
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class RandomLoadBalance implements LoadBalance {

    @Override
    public Instance select(List<Instance> instances) {
        return instances.get(new Random().nextInt(instances.size()));
    }
}
