package com.hanfei.rpc.loadbalancer;

import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobin implements LoadBalancer {
    private final AtomicInteger index = new AtomicInteger(0);
    
    @Override
    public Instance select(List<Instance> instances) {
        int currentIndex = index.getAndIncrement();
        
        // 防止index溢出
        if (currentIndex < 0) {
            index.set(0);
            currentIndex = index.getAndIncrement();
        }
        
        // 取模轮询
        return instances.get(currentIndex % instances.size());
    }
}
