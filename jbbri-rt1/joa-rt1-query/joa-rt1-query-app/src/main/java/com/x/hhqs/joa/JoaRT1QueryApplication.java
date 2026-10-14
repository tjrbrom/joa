package com.x.hhqs.joa;

import java.time.ZoneId;
import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JoaRT1QueryApplication {

	public static void main(String[] args) {
		SpringApplication.run(JoaRT1QueryApplication.class, args);
	}

	@PostConstruct
	public void init() {
		// Setting Spring Boot SetTimeZone
		ZoneId zoneId = ZoneId.of("Asia/Shanghai");
		TimeZone.setDefault(TimeZone.getTimeZone(zoneId));
	}

}
