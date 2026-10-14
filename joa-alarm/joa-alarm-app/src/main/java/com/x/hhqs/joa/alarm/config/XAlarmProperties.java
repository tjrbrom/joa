package com.x.hhqs.joa.alarm.config;

import com.x.hhqs.joa.alarm.dto.AlarmType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@ConfigurationProperties(prefix = "joa-alarm.xalarm2joaalarm")
public class XAlarmProperties {

    private Map<String, String> categoryMapping;
    private Set<String> allowedOrigins;

    public Set<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        if (allowedOrigins != null) {
            this.allowedOrigins = new HashSet<>(allowedOrigins);
        } else {
            this.allowedOrigins = new HashSet<>();
        }
    }

    public Map<String, String> getCategoryMapping() {
        return this.categoryMapping;
    }

    public void setCategoryMapping(Map<String, String> categoryMapping) {
        this.categoryMapping = categoryMapping;
    }

    public String getOmcAlarmType(String category) {
        return getCategoryMapping().getOrDefault(category, AlarmType.ProcessingFailure.name());
    }
}