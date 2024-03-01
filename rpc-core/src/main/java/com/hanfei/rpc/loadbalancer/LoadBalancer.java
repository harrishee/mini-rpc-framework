package com.hanfei.rpc.loadbalancer;

import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;

public interface LoadBalancer {
    // 从服务实例列表中选择一个
    // TODO: 为了一致性哈希算法，可能要新增一个 key 参数
    Instance select(List<Instance> instances);
}
