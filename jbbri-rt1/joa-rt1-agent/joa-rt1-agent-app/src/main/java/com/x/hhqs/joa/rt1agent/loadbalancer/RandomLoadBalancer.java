package com.x.hhqs.joa.rt1agent.loadbalancer;

import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public class RandomLoadBalancer implements LoadBalancer {
    private final Random random = new Random();
    @Override
    public Optional<Instance> chooseInstance(List<Instance> instanceList) {
        if (instanceList == null || instanceList.isEmpty()) {
            return Optional.empty();
        }
        int randomIndex = random.nextInt(instanceList.size());
        return Optional.ofNullable(instanceList.get(randomIndex));
    }
}
