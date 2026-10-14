package com.x.hhqs.joa.alarm.config;

import com.google.common.eventbus.AsyncEventBus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class EventBusConfig {

    private final ThreadPoolExecutor centralisedThreadPoolExecutor;

    public EventBusConfig(){
        centralisedThreadPoolExecutor =  new ThreadPoolExecutor(3, 5, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10), new ThreadPoolExecutor.DiscardPolicy());
    }

    @Bean
    public AsyncEventBus initEventBus(){
       return new AsyncEventBus(centralisedThreadPoolExecutor);
    }

}
