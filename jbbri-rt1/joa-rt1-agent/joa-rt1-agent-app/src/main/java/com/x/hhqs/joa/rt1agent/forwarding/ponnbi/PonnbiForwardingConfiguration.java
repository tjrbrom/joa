package com.x.hhqs.joa.rt1agent.forwarding.ponnbi;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.netty.bootstrap.Bootstrap;

@Configuration
@EnableConfigurationProperties({PonnbiProperties.class})
public class PonnbiForwardingConfiguration {

    @Bean
    public PonnbiClientManager ponnbiClientManager(PonnbiProperties ponnbiProperties) {
        return new PonnbiClientManager(ponnbiProperties, new Bootstrap());
    }

    @Bean
    public PonnbiProxy ponnbiProxy(PonnbiClientManager ponnbiClientManager, PonnbiProperties ponnbiProperties) {
        return new PonnbiProxy(ponnbiClientManager, ponnbiProperties);
    }

}
