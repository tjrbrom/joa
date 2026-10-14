package com.x.hhqs.joa.rmuid.es;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.nio.conn.NoopIOSessionStrategy;
import org.apache.http.nio.conn.SchemeIOSessionStrategy;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.nio.reactor.IOReactorExceptionHandler;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.RequestOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.ClientConfiguration.TerminalClientConfigurationBuilder;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchClients;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.util.StringUtils;
import com.x.hhqs.joa.rmuid.api.QueryService;
import com.x.hhqs.joa.rmuid.service.IsQueryService;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.transport.TransportOptions;
import co.elastic.clients.transport.rest_client.RestClientOptions;
import co.elastic.clients.util.ContentType;

@Configuration
@ConditionalOnClass(ElasticsearchConfiguration.class)
@ConditionalOnProperty(name = "joa.es.client.enabled", havingValue = "true", matchIfMissing = false)
@EnableConfigurationProperties({EsProperties.class})
public class EsClientConfigConfiguration extends ElasticsearchConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(EsClientConfigConfiguration.class);

    private static final long KEEP_ALIVE_DURATION_MINUTES = 30;

    @Autowired
    private EsProperties esProperties;

    @Override
    public ClientConfiguration clientConfiguration() {
        Function<HttpAsyncClientBuilder, HttpAsyncClientBuilder> httpClientBuilderCallback = builder -> {
            builder.setKeepAliveStrategy(
                (response, context) -> TimeUnit.MINUTES.toMillis(KEEP_ALIVE_DURATION_MINUTES))
                .addInterceptorLast((HttpResponseInterceptor) (response, context) -> response
                    .addHeader("X-Elastic-Product", "Elasticsearch"));

            try {
                builder.setConnectionManager(createPoolingNHttpClientConnectionManager(getSSL()));
            } catch (IOReactorException e) {
                throw new RuntimeException(e);
            }
            return builder;
        };

        TerminalClientConfigurationBuilder builder = ClientConfiguration.builder()
            .connectedTo(esProperties.getHosts().toArray(new String[0]))
            .usingSsl(getSSL(), (hostname, session) -> true)
            .withBasicAuth(esProperties.getUser(), esProperties.getPassword())
            .withClientConfigurer(ElasticsearchClients.ElasticsearchClientConfigurationCallback
                .from(httpClientBuilderCallback));

        if (StringUtils.hasText(esProperties.getPathPrefix())) {
            builder = builder.withPathPrefix(esProperties.getPathPrefix());
        }

        return builder.build();
    }

    PoolingNHttpClientConnectionManager createPoolingNHttpClientConnectionManager(SSLContext sslcontext)
        throws IOReactorException {
        DefaultConnectingIOReactor ioReactor = new DefaultConnectingIOReactor();
        addExceptionCallbackHandler(ioReactor);
        HostnameVerifier userTokenHandler = new HostnameVerifier() {
            @Override
            public boolean verify(String host, SSLSession session) {
                return true;
            }
        };
        SchemeIOSessionStrategy sslStrategy = new SSLIOSessionStrategy(sslcontext, userTokenHandler);
        RegistryBuilder<SchemeIOSessionStrategy> builder = RegistryBuilder.create();
        Registry<SchemeIOSessionStrategy> iosessionFactoryRegistry = builder
            .register("http", NoopIOSessionStrategy.INSTANCE)
            .register("https", sslStrategy)
            .build();

        return new PoolingNHttpClientConnectionManager(ioReactor, iosessionFactoryRegistry);
    }

    private void addExceptionCallbackHandler(DefaultConnectingIOReactor ioReactor) {
        ioReactor.setExceptionHandler(new IOReactorExceptionHandler() {
            @Override
            public boolean handle(IOException e) {
                LOGGER.warn("IOReactor encountered a checked IOException", e);
                return true; // Return true to note this exception as handled, it will not be re-thrown
            }

            @Override
            public boolean handle(RuntimeException e) {
                LOGGER.warn("IOReactor encountered a runtime exception", e);
                return true; // Return true to note this exception as handled, it will not be re-thrown
            }
        });
    }
    
    SSLContext getSSL() {
        TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;
        SSLContext sslContext = null;
        try {
            sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
        } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return sslContext;
    }

    @Override
    public TransportOptions transportOptions() {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
        builder.addHeader("Accept", ContentType.APPLICATION_JSON);
        builder.addHeader("Content-Type", ContentType.APPLICATION_JSON);
        return new RestClientOptions(builder.build());
    }

    @Bean
    @ConditionalOnMissingBean(QueryService.class)
    public QueryService queryService(ElasticsearchClient esClient) {
        return new IsQueryService(esClient);
    }
}
