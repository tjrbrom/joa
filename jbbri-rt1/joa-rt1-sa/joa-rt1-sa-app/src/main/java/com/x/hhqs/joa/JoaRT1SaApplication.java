package com.x.hhqs.joa;

import java.time.ZoneId;
import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JoaRT1SaApplication {

	public static void main(String[] args) {
		 SpringApplication.run(JoaRT1SaApplication.class, args);
	}

	@PostConstruct
	public void init() {
		// Setting Spring Boot SetTimeZone
		ZoneId zoneId = ZoneId.of("Asia/Shanghai");
		TimeZone.setDefault(TimeZone.getTimeZone(zoneId));
	}

}
