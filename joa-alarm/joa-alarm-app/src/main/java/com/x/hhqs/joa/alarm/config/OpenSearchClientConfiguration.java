package com.x.hhqs.joa.alarm.config;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContextBuilder;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.rest_client.RestClientTransport;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;

import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

@Configuration
@EnableConfigurationProperties(OpenSearchProperties.class)
@ConditionalOnProperty(name = { "url", "username", "password" }, prefix = "core.opensearch")
public class OpenSearchClientConfiguration {
    
    private final OpenSearchProperties openSearchProperties;

    public OpenSearchClientConfiguration(OpenSearchProperties openSearchProperties) {
        this.openSearchProperties = openSearchProperties;
    }

    @Bean
    public RestClient restClient() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(openSearchProperties.getUsername(),
                        openSearchProperties.getPassword()));

        SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial((chain, authType) -> true)
                .build();

        URI uri = URI.create(openSearchProperties.getUrl());
        HttpHost httpHost = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
        RestClientBuilder restClientBuilder = RestClient.builder(httpHost)
                .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
                        .setDefaultCredentialsProvider(credentialsProvider)
                        .setSSLContext(sslContext)
                        .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE));

        if (uri.getPath() != null && !uri.getPath().isBlank()) {
            restClientBuilder.setPathPrefix(uri.getPath());
        }

        return restClientBuilder.build();
    }

    @Bean
    public OpenSearchClient openSearchClient() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        final OpenSearchTransport transport = new RestClientTransport(restClient(), new JacksonJsonpMapper());
        return new OpenSearchClient(transport);
    }
}
