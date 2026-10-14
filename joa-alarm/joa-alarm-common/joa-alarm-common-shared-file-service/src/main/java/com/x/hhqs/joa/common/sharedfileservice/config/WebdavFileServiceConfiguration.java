package com.x.hhqs.joa.common.sharedfileservice.config;

import com.x.hhqs.joa.common.sharedfileservice.api.SharedFileService;
import com.x.hhqs.joa.common.sharedfileservice.impl.WebdavFileService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;


@Configuration
@EnableConfigurationProperties(WebdavFileServiceProperties.class)
@ConditionalOnProperty(name = {
        "url", "username", "password", "instance-prefix",
        "instance-name", "shared-file-server-external-url", "k8s-public-ip",
        "staging-area"
}
        ,prefix = "core.webdav")
public class WebdavFileServiceConfiguration {

    private final WebdavFileServiceProperties webdavFileServiceProperties;

    public WebdavFileServiceConfiguration(WebdavFileServiceProperties webdavFileServiceProperties) {
        this.webdavFileServiceProperties = webdavFileServiceProperties;
    }

    @Bean
    public SharedFileService sharedFileService() throws NoSuchAlgorithmException, KeyManagementException {
        WebdavFileService webdavFileService = new WebdavFileService(webdavFileServiceProperties);
        return webdavFileService;
    }
}
