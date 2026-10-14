package com.x.hhqs.joa.common.message;

import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;

public class JoaMessage {

    @Value("${joa.message.language:zh}")
    private Locale locale;

    private MessageSource messageSource;

    private static JoaMessage instance;

    public JoaMessage(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Autowired
    public void setInstance(JoaMessage instance) {
        JoaMessage.instance = instance;
    }

    public static JoaMessage instance() {
        return instance;
    }

    public String getMessage(String code) {
        return this.getMessage(code, code);
    }

    public String getMessage(String code, String defaultMessage) {
        return messageSource.getMessage(code, null, defaultMessage, locale);
    }

    public String getMessage(String code, Object[] args) {
        return this.getMessage(code, args, code);
    }

    public String getMessage(String code, Object[] args, String defaultMessage) {
        return messageSource.getMessage(code, args, defaultMessage, locale);
    }

}