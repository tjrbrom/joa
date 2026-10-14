package com.x.hhqs.joa.alarm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component
public class AsyncActiveAlarmFileTaskManager {

    @Autowired
    @Qualifier("asyncActiveAlarmTaskExecutor")
    private ThreadPoolTaskExecutor asyncActiveAlarmTaskExecutor;

    public void execute(Runnable task) {
        asyncActiveAlarmTaskExecutor.execute(task);
    }
}
