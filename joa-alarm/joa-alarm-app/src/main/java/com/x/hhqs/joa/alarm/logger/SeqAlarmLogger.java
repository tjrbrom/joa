package com.x.hhqs.joa.alarm.logger;

import ch.qos.logback.classic.Logger;
import com.x.hhqs.joa.alarm.vo.OmcAlarmVO;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SeqAlarmLogger {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(SeqAlarmLogger.class);

    public static void send(List<OmcAlarmVO> alarms) {
        alarms.forEach(alarm -> {
            LOGGER.info("{},{}", alarm.getChannelIndex(), alarm.getJsonValue());
        });
    }
}
