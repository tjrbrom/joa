package com.x.hhqs.joa.rt1agent;

import java.time.ZoneId;
import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.x.hhqs.joa.common.message.EnableJoaMessage;

@SpringBootApplication
@EnableScheduling
@EnableJoaMessage
public class JoaRT1AgentApplication {

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
		SpringApplication.run(JoaRT1AgentApplication.class, args);
	}

	@PostConstruct
	public void init() {
		// Setting Spring Boot SetTimeZone
		ZoneId zoneId = ZoneId.of("Asia/Shanghai");
		TimeZone.setDefault(TimeZone.getTimeZone(zoneId));
	}

	@Bean
	public TaskExecutor taskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(10);
		executor.setMaxPoolSize(20);
		// executor.setQueueCapacity(50);
		return executor;
	}

}
