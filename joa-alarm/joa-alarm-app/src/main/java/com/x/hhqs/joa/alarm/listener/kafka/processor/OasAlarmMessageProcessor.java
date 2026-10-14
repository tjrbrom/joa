package com.x.hhqs.joa.alarm.listener.kafka.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.x.hhqs.joa.alarm.config.JoaAlarmConfig;
import com.x.hhqs.joa.alarm.dto.JoaAlarmInfo;
import com.x.hhqs.joa.alarm.dto.OASAlarmMessageInfo;
import com.x.hhqs.joa.alarm.dto.OasAlarmInfo;
import com.x.hhqs.joa.alarm.entity.JoaAlarm;
import com.x.hhqs.joa.alarm.utils.ChannelIndexUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component("oasAlarmMessageProcessor")
public class OasAlarmMessageProcessor implements AlarmMessageProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(OasAlarmMessageProcessor.class);

    private final ObjectMapper objectMapper;
    private final JoaAlarmConfig joaAlarmConfig;
    private final AsyncActiveAlarmMessageProcessor asyncActiveAlarmMessageProcessor;


    public OasAlarmMessageProcessor(ObjectMapper objectMapper, JoaAlarmConfig joaAlarmConfig,AsyncActiveAlarmMessageProcessor asyncActiveAlarmMessageProcessor) {
        this.objectMapper = objectMapper;
        this.joaAlarmConfig = joaAlarmConfig;
        this.asyncActiveAlarmMessageProcessor = asyncActiveAlarmMessageProcessor;
    }

    @Override
    public List<JoaAlarm> process(String message) {
        {
            try {
                OASAlarmMessageInfo oasAlarmMessageInfo = objectMapper.readValue(message, OASAlarmMessageInfo.class);
                if (!ObjectUtils.isEmpty(oasAlarmMessageInfo.getReqId()) && oasAlarmMessageInfo.getAckSyncAlarmFileResult() != null) {
                    LOGGER.info("listener async active alarm message success! reqId-{}, status-{}", oasAlarmMessageInfo.getReqId(), oasAlarmMessageInfo.ackSyncAlarmFileResult.getStatus().name());
                    asyncActiveAlarmMessageProcessor.process(oasAlarmMessageInfo);
                    return List.of();
                }
                List<OasAlarmInfo> oasAlarmInfos = oasAlarmMessageInfo.getOmcAlarmVOs();

                List<JoaAlarm> joaAlarms = new ArrayList<>();
                for (OasAlarmInfo vo : oasAlarmInfos) {
                    String alarmId = vo.getAlarmId();
                    String neName = vo.getNeName();
                    JoaAlarmInfo info = new JoaAlarmInfo();
                    info.setAlarmId(alarmId);
                    info.setAlarmTitle(vo.getAlarmTitle());
                    info.setAlarmStatus(vo.getAlarmStatus());
                    info.setAlarmType(vo.getAlarmType());
                    info.setOrigSeverity(vo.getOrigSeverity());
                    info.setEventTime(vo.getEventTime());
                    info.setSpecificProblemID(vo.getSpecificProblemID());
                    info.setSpecificProblem(vo.getSpecificProblem());
                    info.setNeUID(vo.getNeUID());
                    info.setNeName(neName);
                    info.setNeType(vo.getNeType());
                    info.setObjectUID(vo.getObjectUID());
                    info.setObjectName(vo.getObjectName());
                    info.setObjectType(vo.getObjectType());
                    info.setLocationInfo(vo.getLocationInfo());
                    info.setHolderType(vo.getHolderType());
                    info.setAlarmCheck(vo.getAlarmCheck());
                    info.setLayer(vo.getLayer());
                    info.setAddInfo(vo.getAddInfo());
                    info.setRelateType(vo.getRelateType());
                    info.setCorrelatedSourceAlarmSN(vo.getCorrelatedSourceAlarmSN());
                    info.setMaintenanceStatus(vo.getMaintenanceStatus());
                    info.setLocation(vo.getLocation());
                    String neIP = vo.getNeIP();
                    int channelIndex = 0;
                    if (!"OMC".equals(vo.getNeType())) {
                        channelIndex = ChannelIndexUtil.getChannelIndex(neIP, joaAlarmConfig.getServer().getChannelNumber());
                    }
                    JoaAlarm joaAlarm = new JoaAlarm()
                            .alarmId(alarmId)
                            .channelIndex(channelIndex)
                            .createTime(Timestamp.valueOf(vo.getEventTime()))
                            .neName(neName)
                            .jsonValue(info);
                    joaAlarms.add(joaAlarm);
                    LOGGER.debug("Parsed alarm: alarmId={}, neName={}, channelIndex={}",
                            alarmId, neName, channelIndex);
                }
                return joaAlarms;
            } catch (JsonProcessingException e) {
                LOGGER.error("Failed to parse Kafka message: {}", message, e);
                throw new RuntimeException("JSON parse error", e);
            }
        }
    }
}
