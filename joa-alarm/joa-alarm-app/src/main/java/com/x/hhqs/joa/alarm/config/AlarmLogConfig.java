package com.x.hhqs.joa.alarm.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "joa-alarm.alarm-log")
@RefreshScope
@Getter
@Setter
public class AlarmLogConfig {

    private String alarmDataMaxHistory = "3";

    private String oasAlarmMaxHistory = "30";

}
