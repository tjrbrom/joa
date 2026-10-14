package com.x.hhqs.joa.alarm;

import com.x.hhqs.joa.common.restclient.RestClientConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;
import java.time.ZoneId;
import java.util.TimeZone;

@EnableKafka
@EnableScheduling
@SpringBootApplication
@Import(RestClientConfiguration.class)
public class JOAAlarmApplication {
    public static void main(String[] args) {
        SpringApplication.run(JOAAlarmApplication.class, args);
    }

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("Asia/Shanghai")));
    }
}
