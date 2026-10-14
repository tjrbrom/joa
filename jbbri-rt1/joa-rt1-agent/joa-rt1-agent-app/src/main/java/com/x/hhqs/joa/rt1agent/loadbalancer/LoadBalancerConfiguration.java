package com.x.hhqs.joa.rt1agent.loadbalancer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoadBalancerConfiguration {

    @Value("${joa.rt1agent.load-balance.policy:RANDOM}")
    private String policy;

    @Bean
    public LoadBalancer loadBalancer() {
        if (LoadBalancerPolicy.RANDOM.name().equals(policy)) {
            return new RandomLoadBalancer();
        } else if (LoadBalancerPolicy.ROUND_ROBIN.name().equals(policy)) {
            return new RoundRobinLoadBalancer();
        } else {
            throw new IllegalArgumentException("Unknown load balancer policy: " + policy);
        }
    }
}
