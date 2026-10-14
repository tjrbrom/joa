package com.x.hhqs.joa.common.restclient;

import com.x.hhqs.joa.common.restclient.config.RestClientConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RestClientProperties.class)
@ConditionalOnProperty(name = {"baseUrl", "username", "password"}, prefix = "core.rest")
public class RestClientConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestClientConfiguration.class);

    private final RestClientProperties restClientProperties;

    public RestClientConfiguration(RestClientProperties restClientProperties) {
        this.restClientProperties = restClientProperties;
    }

    @Bean(initMethod = "login", destroyMethod = "logout")
    public RestClient restconfAuthRestClient() {
        LOGGER.info("RestClient init...");
        return HttpClientFactory.newRestClient(RestClient.class, restClientConfig(), httpMonitor());
    }

    public RestClientConfig restClientConfig() {
        if (StringUtils.isEmpty(restClientProperties.getBaseUrl())) {
            LOGGER.error("Baseurl is required!");
        }
        if (StringUtils.isEmpty(restClientProperties.getUsername())) {
            LOGGER.error("Username is required!");
        }
        if (StringUtils.isEmpty(restClientProperties.getPassword())) {
            LOGGER.error("Password is required!");
        }
        RestClientConfig.RestClientConfigBuilder builder = RestClientConfig.builder();
        builder.baseUrl(restClientProperties.getBaseUrl());
        builder.sslSocketFactory(getSSLConnectionSocketFactory());
        builder.credential(restClientProperties.getUsername(), restClientProperties.getPassword());
        if (restClientProperties.getTokenRefreshTime() != 0) {
            builder.tokenRefreshTime(restClientProperties.getTokenRefreshTime());
        }
        if (restClientProperties.getConnectTimeout() != 0) {
            builder.connectTimeout(restClientProperties.getConnectTimeout());
        }
        if (restClientProperties.getReadTimeout() != 0) {
            builder.readTimeout(restClientProperties.getReadTimeout());
        }
        if (restClientProperties.getMaxTotalConnections() != 0) {
            builder.maxTotalConnections(restClientProperties.getMaxTotalConnections());
        }
        if (restClientProperties.getMaxConnectionsPerRoute() != 0) {
            builder.maxConnectionsPerRoute(restClientProperties.getMaxConnectionsPerRoute());
        }
        return builder.build();
    }

    private HttpMonitor httpMonitor() {
        return new DefaultHttpMonitor();
    }

    private SSLConnectionSocketFactory getSSLConnectionSocketFactory() {
        try {
            return new SSLConnectionSocketFactory(new SSLContextBuilder()
                    .loadTrustMaterial(null, (x509Certificates, s) -> true).build(),
                    NoopHostnameVerifier.INSTANCE);
        } catch (Exception e) {
            LOGGER.error("Error while constructing HTTPS pool", e);
        }
        return null;
    }
}
