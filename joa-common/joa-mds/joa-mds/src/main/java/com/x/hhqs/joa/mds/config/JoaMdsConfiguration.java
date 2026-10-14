package com.x.hhqs.joa.mds.config;

import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.x.hhqs.joa.mds.crypto.CryptoService;
import com.x.hhqs.joa.mds.crypto.EmptyCryptoService;
import com.x.hhqs.joa.mds.crypto.MdsCryptoService;
import com.x.hhqs.joa.mds.emsclient.ams.AmsRestClient;
import com.x.hhqs.joa.mds.emsclient.ams.AmsRestClientConfig;
import com.x.hhqs.joa.mds.sync.CredentialService;
import com.x.hhqs.joa.mds.utils.WebContextUtils;

@Configuration
@EnableConfigurationProperties(AmsProperties.class)
public class JoaMdsConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(JoaMdsConfiguration.class);

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        // objectMapper.registerModule(new JavaTimeModule());
        // objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return objectMapper;
    }

    @Bean
    public WebClient webClient() {
        return WebContextUtils.createWebClientWithoutSSL();
    }

    @Bean
    public Function<AmsRestClientConfig, AmsRestClient> amsRestClientFactory(WebClient webClient,
        ObjectMapper objectMapper) {
        return config -> {
            AmsRestClient client = new AmsRestClient(config);
            client.setWebClient(webClient);
            client.setObjectMapper(objectMapper);

            return client;
        };
    }

    @Bean
    public CredentialService credentialService(CryptoService cryptoService, AmsProperties amsProperties) {
        return new CredentialService(cryptoService, amsProperties);
    }

    @Bean
    @ConditionalOnResource(resources = "${secretkey.path}")
    public CryptoService cryptoService() {
        return new MdsCryptoService();
    }

    @Bean
    @ConditionalOnMissingBean(CryptoService.class)
    public CryptoService emptyCryptoService(@Value("${secretkey.path:}") String path) {
        LOGGER.warn("Resources not exist, secretkey.path: [{}]", path);
        return new EmptyCryptoService();
    }

}
