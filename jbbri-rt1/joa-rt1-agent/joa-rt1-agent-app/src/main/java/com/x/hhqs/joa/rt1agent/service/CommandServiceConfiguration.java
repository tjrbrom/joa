package com.x.hhqs.joa.rt1agent.service;

import com.x.hhqs.joa.rt1agent.loadbalancer.LoadBalancer;
import com.x.hhqs.joa.rt1agent.router.Router;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class CommandServiceConfiguration {
    @Autowired
    private CommandServiceManager serviceManager;

    @Autowired(required = false)
    private Router router;

    @Autowired
    private LoadBalancer loadBalancer;

    // @Autowired
    // @Lazy 
    // private RestTemplate restClient;

    @Bean
    public RT1CommandService rt1CommandService(RestTemplate restClient) {
        CommandServiceBuilder.Builder builder = new CommandServiceBuilder.Builder();
        return builder.serviceManager(serviceManager)
                .router(router)
                .loadBalancer(loadBalancer)
                .restClient(restClient)
                .proxy(RT1CommandService.class)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
