package com.x.hhqs.joa.alarm.service.impl;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class AlarmInitStatusHolder {
    private final AtomicBoolean isReady = new AtomicBoolean(false);
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    public boolean casReady(boolean expectedValue, boolean newValue) {
        return isReady.compareAndSet(expectedValue, newValue);
    }

    public boolean isReady() {
        return isReady.get();
    }

    public void setReady(boolean ready) {
        this.isReady.set(ready);
    }

    public boolean casRunning(boolean expectedValue, boolean newValue) {
        return isRunning.compareAndSet(expectedValue, newValue);
    }

    public boolean isRunning() {
        return isRunning.get();
    }

    public void setRunning(boolean running) {
        this.isRunning.set(running);
    }
}