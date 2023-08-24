package com.hanfei.rpc.loadbalance;

import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;

/**
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class RoundRobinLoadBalance implements LoadBalance {

    private int index = 0;

    @Override
    public Instance select(List<Instance> instances) {
        // 如果索引超过实例列表的大小
        if (index >= instances.size()) {
            // 将索引回滚到合法范围内
            index %= instances.size();
        }
        // 获取当前索引对应的实例，并将索引递增
        return instances.get(index++);
    }
}
