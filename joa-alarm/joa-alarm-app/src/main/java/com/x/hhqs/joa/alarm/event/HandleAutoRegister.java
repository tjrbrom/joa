package com.x.hhqs.joa.alarm.event;

import com.google.common.eventbus.AsyncEventBus;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class HandleAutoRegister implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();
        Map<String, IEventHandler> types = applicationContext.getBeansOfType(IEventHandler.class);
        AsyncEventBus bean = applicationContext.getBean(AsyncEventBus.class);
        types.values().forEach(bean::register);
    }
}
