package com.x.hhqs.joa.alarm.listener.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.x.hhqs.joa.alarm.dto.JoaAlarmInfo;
import com.x.hhqs.joa.alarm.entity.JoaAlarm;
import com.x.hhqs.joa.alarm.event.OmcAlarmEvent;
import com.x.hhqs.joa.alarm.exception.DependencyNotReadyException;
import com.x.hhqs.joa.alarm.exception.UnsupportedAlarmException;
import com.x.hhqs.joa.alarm.listener.kafka.processor.AlarmMessageProcessor;
import com.x.hhqs.joa.alarm.service.JoaAlarmDaoService;
import com.x.hhqs.joa.alarm.service.manager.ChannelAlarmSeqManager;
import com.x.hhqs.joa.alarm.utils.JsonUtils;
import com.x.hhqs.joa.alarm.vo.OmcAlarmVO;

import com.google.common.eventbus.AsyncEventBus;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class KafkaAlarmConsumerListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaAlarmConsumerListener.class);

    private final KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;
    private final ChannelAlarmSeqManager channelAlarmSeqManager;
    private final JoaAlarmDaoService joaAlarmDaoService;
    private final AsyncEventBus asyncEventBus;
    private final AlarmMessageProcessor oasAlarmMessageProcessor;
    private final AlarmMessageProcessor xCoreAlarmMessageProcessor;

    public KafkaAlarmConsumerListener(KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry,
                                      ChannelAlarmSeqManager channelAlarmSeqManager,
                                      JoaAlarmDaoService joaAlarmDaoService,
                                      AsyncEventBus asyncEventBus,
                                      @Qualifier("oasAlarmMessageProcessor") AlarmMessageProcessor oasAlarmMessageProcessor,
                                      @Qualifier("xCoreAlarmMessageProcessor") AlarmMessageProcessor xCoreAlarmMessageProcessor
    ) {
        this.kafkaListenerEndpointRegistry = kafkaListenerEndpointRegistry;
        this.channelAlarmSeqManager = channelAlarmSeqManager;
        this.joaAlarmDaoService = joaAlarmDaoService;
        this.asyncEventBus = asyncEventBus;
        this.oasAlarmMessageProcessor = oasAlarmMessageProcessor;
        this.xCoreAlarmMessageProcessor = xCoreAlarmMessageProcessor;
    }

    @KafkaListener(
            id = "oas-alarm-listener",
            groupId = "${spring.kafka.consumer.group-id}",
            topics = {"OAS_ALARM"},
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listenOasAlarm(List<ConsumerRecord<String, String>> records, Acknowledgment ack) {
        listen(records, ack, oasAlarmMessageProcessor, "oas-alarm-listener");
    }

    @KafkaListener(
            id = "oas-alarm-sender",
            groupId = "${spring.kafka.consumer.group-id}",
            topics = {"OAS_ALARM_REQ"},
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listenOasReqAlarm(List<ConsumerRecord<String, String>> records, Acknowledgment ack) {

    }
    @KafkaListener(
            id = "x-core-alarm-listener",
            groupId = "${spring.kafka.consumer.group-id}",
            topics = {"${joa-alarm.xalarm2joaalarm.topic:x-core-alarm}"},
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listenXCoreAlarm(List<ConsumerRecord<String, String>> records, Acknowledgment ack) {
        listen(records, ack, xCoreAlarmMessageProcessor, "x-core-alarm-listener");
    }

    public void listen(List<ConsumerRecord<String, String>> records,
                       Acknowledgment ack,
                       AlarmMessageProcessor processor,
                       String listenerId) {

        List<JoaAlarm> allJoaAlarms = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        String batchId = UUID.randomUUID().toString();

        try {
            for (ConsumerRecord<String, String> record : records) {
                try {
                    List<JoaAlarm> alarms = processor.process(record.value());
                    allJoaAlarms.addAll(alarms);
                } catch (DependencyNotReadyException de) {
                    LOGGER.error("Dependency module not ready, stopping Kafka listener: {}", de.getMessage(), de);
                    kafkaListenerEndpointRegistry.getListenerContainer(listenerId).stop();
                    return; // Don't ack
                } catch (UnsupportedAlarmException e) {
                    LOGGER.warn("Unsupported alarm skipped: {}", e.getMessage());
                    // continue and acknowledge the message to avoid blocking
                    continue;
                } catch (Exception e) {
                    LOGGER.error("Could not process message: {}", e.getMessage(), e);
                    throw e; // Let DefaultErrorHandler handle
                }
            }

            if (!allJoaAlarms.isEmpty()) {
                // 1. Group alarms by channel
                Map<Integer, List<JoaAlarm>> channelAlarmMap = allJoaAlarms.stream()
                        .collect(Collectors.groupingBy(JoaAlarm::getChannelIndex));

                // 2. Begin + allocate seq for each channel
                for (Map.Entry<Integer, List<JoaAlarm>> entry : channelAlarmMap.entrySet()) {
                    int channel = entry.getKey();
                    List<JoaAlarm> alarms = entry.getValue();

                    channelAlarmSeqManager.begin(channel);
                    for (JoaAlarm alarm : alarms) {
                        long seq = channelAlarmSeqManager.next(channel);
                        alarm.getJsonValue().setAlarmSeq(seq);
                        alarm.alarmSeq(seq);
                    }
                }

                // 3. Save all alarms to DB
                joaAlarmDaoService.saveBatch(allJoaAlarms);

                // 4. Commit all channel sequences
                for (Integer channel : channelAlarmMap.keySet()) {
                    channelAlarmSeqManager.commit(channel);
                }

                // 5. Publish event after successful commit
                sendAlarmEvent(allJoaAlarms);
                long duration = System.currentTimeMillis() - startTime;
                LOGGER.info("[batchId={}] Successfully processed JOA alarm batch, totalAlarms={}, channels={}, duration={}ms",
                        batchId, allJoaAlarms.size(), channelAlarmMap.size(), duration);
            } else {
                LOGGER.debug("[batchId={}] No alarms to process in this batch.", batchId);
            }

            ack.acknowledge();

        } catch (Exception e) {
            LOGGER.error("[batchId={}] Alarm batch processing failed: {}", batchId, e.getMessage(), e);

            // rollback all channel sequences
            Set<Integer> involvedChannels = allJoaAlarms.stream()
                    .map(JoaAlarm::getChannelIndex)
                    .collect(Collectors.toSet());

            for (Integer channel : involvedChannels) {
                try {
                    channelAlarmSeqManager.rollback(channel);
                } catch (Exception rollbackEx) {
                    LOGGER.error("[batchId={}] Rollback failed for channel {}: {}", batchId, channel, rollbackEx.getMessage(), rollbackEx);
                }
            }

            throw e; // Let DefaultErrorHandler retry or send to DLT
        }
    }

    public void sendAlarmEvent(List<JoaAlarm> joaAlarms) {
        LOGGER.info("Preparing to send alarm event, total alarms: {}", joaAlarms.size());
        List<OmcAlarmVO> omcAlarmVOS = joaAlarms.stream().map(joaAlarm -> {
            OmcAlarmVO omcAlarmVO = new OmcAlarmVO();
            omcAlarmVO.setId(joaAlarm.getId());
            omcAlarmVO.setAlarmSeq(joaAlarm.getAlarmSeq());
            omcAlarmVO.setChannelIndex(joaAlarm.getChannelIndex());
            omcAlarmVO.setCreateTime(joaAlarm.getCreateTime());
            omcAlarmVO.setJsonValue(toJson(joaAlarm.getJsonValue()));
            omcAlarmVO.setNeName(joaAlarm.getNeName());
            omcAlarmVO.setAlarmId(String.valueOf(joaAlarm.getAlarmId()));
            return omcAlarmVO;
        }).toList();

        OmcAlarmEvent omcAlarmEvent = new OmcAlarmEvent();
        omcAlarmEvent.setOmcAlarmVOList(omcAlarmVOS);
        asyncEventBus.post(omcAlarmEvent);
        LOGGER.info("Alarm event posted successfully with {} OmcAlarmVOs", omcAlarmVOS.size());
    }

    private String toJson(JoaAlarmInfo alarm) {
        try {
            return JsonUtils.getInstance().toJson(alarm);
        } catch (JsonProcessingException e) {
            LOGGER.error("Mapper to json fail, alarm id=" + alarm.getAlarmId(), e);
            return null;
        }
    }
}
