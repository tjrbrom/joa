package com.x.hhqs.joa.alarm.listener.kafka;

import com.x.hhqs.joa.alarm.service.impl.AlarmDefinitionInitializer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CoreInternalInvCollectionConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(CoreInternalInvCollectionConsumer.class);

    private static final String DEVICE_PLUG_DEPLOYED = "device-plug-deployed";
    private static final String DEVICE_PLUG_UNDEPLOYED = "device-plug-undeployed";

    private final AlarmDefinitionInitializer alarmDefinitionInitializer;

    public CoreInternalInvCollectionConsumer(AlarmDefinitionInitializer alarmDefinitionInitializer) {
        this.alarmDefinitionInitializer = alarmDefinitionInitializer;
    }

    @KafkaListener(
            id = "core-internal-inv-collection-model-listener",
            groupId = "${spring.kafka.consumer.group-id}",
            topics = {"${joa-alarm.core-internal-inv-topic:CORE_INTERNAL_INVENTORY_COLLECTION_MODELS}"},
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleDevicePlugNotification(List<ConsumerRecord<String, String>> records, Acknowledgment ack) {
        for (ConsumerRecord<String, String> record : records) {
            String kafkaNotifXml = record.value();
            if (kafkaNotifXml.contains(DEVICE_PLUG_DEPLOYED)) {
                alarmDefinitionInitializer.submit();
            } else if (kafkaNotifXml.contains(DEVICE_PLUG_UNDEPLOYED)) {
                LOGGER.info("Skipping device plug undeployed notification : {}", kafkaNotifXml);
            }
            ack.acknowledge();
        }
    }
}
