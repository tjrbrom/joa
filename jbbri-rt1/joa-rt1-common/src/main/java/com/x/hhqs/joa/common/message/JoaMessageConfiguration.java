package com.x.hhqs.joa.common.message;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

@Configuration
public class JoaMessageConfiguration {

    @Value("${joa.message.basenames:classpath:i18n/messages,classpath:i18n/common}")
    private String[] basenames;
    
    @Value("${joa.message.encoding:UTF-8}")
    private String encoding;
    
    @Bean
    public MessageSource joaMessageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames(basenames);
        messageSource.setDefaultEncoding(encoding);
        return messageSource;
    }

    @Bean
    @RefreshScope
    public JoaMessage joaMessage() {
        return new JoaMessage(joaMessageSource());
    }
}