package com.x.hhqs.joa.alarm.listener.kafka;

import com.x.hhqs.joa.alarm.service.impl.AlarmDefinitionInitializer;
import com.x.hhqs.joa.alarm.service.manager.ChannelAlarmSeqManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

@Component
public class KafkaListenerMonitor {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaListenerMonitor.class);

    private final ThreadPoolTaskScheduler kafkaListenerMonitorScheduler;
    private final KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;
    private final ChannelAlarmSeqManager channelAlarmSeqManager;
    private final AlarmDefinitionInitializer alarmDefinitionInitializer;

    public KafkaListenerMonitor(ThreadPoolTaskScheduler kafkaListenerMonitorScheduler, KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry,
                                ChannelAlarmSeqManager channelAlarmSeqManager,
                                AlarmDefinitionInitializer alarmDefinitionInitializer) {
        this.kafkaListenerMonitorScheduler = kafkaListenerMonitorScheduler;
        this.kafkaListenerEndpointRegistry = kafkaListenerEndpointRegistry;
        this.channelAlarmSeqManager = channelAlarmSeqManager;
        this.alarmDefinitionInitializer = alarmDefinitionInitializer;
    }

    private boolean checkInitAlarmDefinition() {
        boolean isOk = alarmDefinitionInitializer.isReady();
        if (!isOk) {
            LOGGER.warn("Alarm definition is not ok");
            return isOk;
        }
        LOGGER.debug("Alarm definition is ok");
        // Lightweight check, for example try getting a connection from the pool
        return isOk;
    }

    private boolean checkGenAlarmSeq() {
        boolean isOk = channelAlarmSeqManager.isReady();
        if (!isOk) {
            LOGGER.warn("Alarm seq is not ok");
            return isOk;
        }
        LOGGER.debug("Alarm seq is ok");
        // Lightweight check, for example try getting a connection from the pool
        return isOk;
    }

    @PostConstruct
    public void initMonitor() {
        kafkaListenerMonitorScheduler.scheduleWithFixedDelay(
                () -> this.startKafkaListener("core-internal-inv-collection-model-listener"),
                Instant.now().plusSeconds(10),
                Duration.ofSeconds(10));

        kafkaListenerMonitorScheduler.scheduleWithFixedDelay(
                () -> this.monitorAndRestartAlarmListener("oas-alarm-listener", () ->
                        new HealthCheckReport()
                                .add("genAlarmSeqOk", checkGenAlarmSeq())),
                Instant.now().plusSeconds(10),
                Duration.ofSeconds(10));

        kafkaListenerMonitorScheduler.scheduleWithFixedDelay(
                () -> this.monitorAndRestartAlarmListener("x-core-alarm-listener", () ->
                        new HealthCheckReport()
                                .add("genAlarmSeqOk", checkGenAlarmSeq())
                                .add("alarmDefinitionOk", checkInitAlarmDefinition())),
                Instant.now().plusSeconds(10),
                Duration.ofSeconds(10));
    }

    public void startKafkaListener(String listenerId) {
        MessageListenerContainer container = kafkaListenerEndpointRegistry.getListenerContainer(listenerId);

        if (container == null) {
            throw new IllegalStateException("Kafka listener container '" + listenerId + "' not found. " +
                    "Please check if the listener is correctly defined and registered.");
        }

        if (!container.isRunning()) {
            container.start();
            LOGGER.info("Kafka listener [{}] started.", listenerId);
        }
    }

    public void monitorAndRestartAlarmListener(String listenerId, Supplier<HealthCheckReport> healthCheckSupplier) {
        MessageListenerContainer container = kafkaListenerEndpointRegistry.getListenerContainer(listenerId);

        if (container == null) {
            throw new IllegalStateException("Kafka listener container '" + listenerId + "' not found. " +
                    "Please check if the listener is correctly defined and registered.");
        }

        HealthCheckReport report = healthCheckSupplier.get();

        if (!report.isHealthy()) {
            // Log or trigger alert when dependency check fails
            if (container.isRunning()) {
                container.stop();
                LOGGER.error("Health check failed. Kafka listener [{}] stopped. Details: {}",
                        listenerId, report);
            } else {
                LOGGER.warn("Still unhealthy. Kafka listener [{}] already stopped. Details: {}",
                        listenerId, report);
            }
        } else {
            if (!container.isRunning()) {
                container.start();
                LOGGER.info("All dependencies recovered. Kafka listener [{}] restarted.", listenerId);
            } else {
                LOGGER.debug("All dependencies healthy. Kafka listener [{}] is running normally.", listenerId);
            }
        }
    }
}