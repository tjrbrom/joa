package com.x.hhqs.joa.in.util;

import com.x.hhqs.joa.in.service.JmxClientService;
import org.springframework.stereotype.Component;

@Component
public class JmxClientServiceFactory {
    public JmxClientService create(String host) {
        return new JmxClientService(host);
    }
}
