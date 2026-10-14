package com.x.hhqs.joa.rt1agent.loadbalancer;

import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;
import java.util.Optional;

public interface LoadBalancer {
    Optional<Instance> chooseInstance(List<Instance> instanceList);
}
