package com.x.hhqs.joa.mds.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.x.hhqs.joa.common.restclient.DefaultHttpMonitor;
import com.x.hhqs.joa.common.restclient.HttpClientFactory;
import com.x.hhqs.joa.common.restclient.HttpMonitor;
import com.x.hhqs.joa.common.restclient.RestClientConfiguration;
import com.x.hhqs.joa.common.restclient.RestClientProperties;
import com.x.hhqs.joa.common.restclient.config.RestClientConfig;
import com.x.hhqs.joa.mds.emsclient.ac.MdsRestClient;

@Configuration
@EnableConfigurationProperties(RestClientProperties.class)
@ConditionalOnProperty(name = "core.rest.enabled", havingValue = "true", matchIfMissing = true)
class MdsRestClientConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestClientConfiguration.class);

    private final RestClientProperties restClientProperties;

    public MdsRestClientConfiguration(RestClientProperties restClientProperties) {
        this.restClientProperties = restClientProperties;
    }

    @Bean(initMethod = "login", destroyMethod = "logout")
    @ConditionalOnProperty(name = {"baseUrl", "username", "password"}, prefix = "core.rest")
    public MdsRestClient restconfAuthRestClient() {
        LOGGER.info("MdsRestClient init...");
        return HttpClientFactory.newRestClient(MdsRestClient.class, restClientConfig(), httpMonitor());
    }

    public RestClientConfig restClientConfig() {
        RestClientConfiguration cfg = new RestClientConfiguration(restClientProperties);
        return cfg.restClientConfig();
    }

    private HttpMonitor httpMonitor() {
        return new DefaultHttpMonitor();
    }

}
