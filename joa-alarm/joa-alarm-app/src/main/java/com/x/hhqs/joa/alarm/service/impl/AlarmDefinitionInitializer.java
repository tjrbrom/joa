package com.x.hhqs.joa.alarm.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@RefreshScope
public class AlarmDefinitionInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(AlarmDefinitionInitializer.class);

    private final AtomicBoolean hasPending = new AtomicBoolean(false);

    private final AtomicBoolean initialized = new AtomicBoolean(false);

    private final AlarmInitService alarmInitService;
    private final AlarmInitStatusHolder statusHolder;
    private final ThreadPoolTaskScheduler alarmDefinitionInitScheduler;

    public AlarmDefinitionInitializer(AlarmInitService alarmInitService,
                                            AlarmInitStatusHolder statusHolder,
                                            ThreadPoolTaskScheduler alarmDefinitionInitScheduler) {
        this.alarmInitService = alarmInitService;
        this.statusHolder = statusHolder;
        this.alarmDefinitionInitScheduler = alarmDefinitionInitScheduler;
    }

    @PostConstruct
    public void init() {
        LOGGER.info("[AlarmDefinitionInitTask] Application started, submitting initial task.");
        submit();
    }

    public void submit() {
        if (!initialized.getAndSet(true)) {
            LOGGER.info("[AlarmDefinitionInitTask] Initialization run-once flag triggered.");
        }

        synchronized (this) {
            if (!statusHolder.casRunning(false, true)) {
                hasPending.set(true);
                LOGGER.info("[AlarmDefinitionInitTask] Task already running. Marked as pending.");
            } else {
                LOGGER.info("[AlarmDefinitionInitTask] Task is idle, submitting to worker.");
                alarmDefinitionInitScheduler.submit(this::runTask);
            }
        }
    }

    private void runTask() {
        try {
            LOGGER.info("[AlarmDefinitionInitTask] Begin executing task...");
            manualInitAlarmDefinitions();
            LOGGER.info("[AlarmDefinitionInitTask] Task execution completed.");
        } catch (Exception e) {
            LOGGER.error("[AlarmDefinitionInitTask] Task execution failed", e);
        } finally {
            finishTask();
        }
    }

    private void finishTask() {
        synchronized (this) {
            if (hasPending.getAndSet(false)) {
                LOGGER.info("[AlarmDefinitionInitTask] Found pending task. Re-running...");
                alarmDefinitionInitScheduler.submit(this::runTask);
            } else {
                statusHolder.setRunning(false);
                LOGGER.info("[AlarmDefinitionInitTask] No pending task. Processor is idle.");
            }
        }
    }

    public void manualInitAlarmDefinitions() {
        statusHolder.casReady(true, false);
        LOGGER.info("Start to initialize alarm definition");
        try {
            long start = System.currentTimeMillis();
            alarmInitService.transactionalInit();
            long end = System.currentTimeMillis() - start;
            LOGGER.info("Initialize alarm definitions took '{}' ms", end);
            statusHolder.setReady(true);
        } catch (Exception e) {
            LOGGER.error("Fail to initialize alarm definition", e);
            statusHolder.setReady(false);
        }
    }

    public boolean isReady() {
        return statusHolder.isReady();
    }

    public boolean isRunning() {
        return statusHolder.isRunning();
    }
}
