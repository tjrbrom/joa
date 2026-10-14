package com.x.hhqs.joa.rt1agent.loadbalancer;

import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;
import java.util.Optional;

public class RoundRobinLoadBalancer implements LoadBalancer {
    private int currentIndex = 0;
    @Override
    public Optional<Instance> chooseInstance(List<Instance> instanceList) {
        if (instanceList == null || instanceList.isEmpty()) {
            return Optional.empty();
        }
        Instance selectedServer = instanceList.get(currentIndex);
        currentIndex = (currentIndex + 1) % instanceList.size();
        return Optional.ofNullable(selectedServer);
    }
}
