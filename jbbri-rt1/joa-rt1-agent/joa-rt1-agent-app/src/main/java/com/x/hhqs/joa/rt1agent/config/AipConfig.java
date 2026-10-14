package com.x.hhqs.joa.rt1agent.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@RefreshScope
@Component
public class AipConfig {

    @Value("${aip.base-url}")
    private String baseUrl;

    public String getBaseUrl() {
        return baseUrl;
    }
}