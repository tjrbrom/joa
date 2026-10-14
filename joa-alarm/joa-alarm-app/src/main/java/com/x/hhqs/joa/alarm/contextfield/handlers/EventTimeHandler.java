package com.x.hhqs.joa.alarm.contextfield.handlers;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import com.x.hhqs.joa.alarm.contextfield.AlarmFields;
import com.x.hhqs.joa.alarm.dto.XAlarmContext;
import org.springframework.stereotype.Component;

@Component
@DefaultHandler(field = AlarmFields.EVENT_TIME)
public class EventTimeHandler extends AbstractAttributeHandler {
    private final DateTimeFormatter OUTPUT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public String getId() {
        return AlarmFields.EVENT_TIME;
    }

    @Override
    public String getValue(XAlarmContext context) {
        String eventTime = EMPTY;
        if ("Cleared".equals(context.getXAlarmVO().getAlarmStatus())) {
            eventTime = iso2CustomEventTime(context.getXAlarmVO().getClearedTime());
        } else {
            eventTime = iso2CustomEventTime(context.getXAlarmVO().getRaisedTime());
        }
        return eventTime;
    }

    public String iso2CustomEventTime(String isoTime) {
        return formatAlarmTime(parseAlarmTime(isoTime));
    }

    public String formatAlarmTime(Timestamp ts) {
        if (ts == null) return null;
        return ts.toLocalDateTime().format(OUTPUT_FORMAT);
    }

    public Timestamp parseAlarmTime(String isoTime) {
        if (isoTime == null || isoTime.isBlank()) {
            return null;
        }
        OffsetDateTime odt = OffsetDateTime.parse(isoTime);
        return Timestamp.valueOf(odt.toLocalDateTime());
    }
}