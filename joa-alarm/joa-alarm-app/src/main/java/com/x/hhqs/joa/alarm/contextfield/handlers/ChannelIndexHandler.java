package com.x.hhqs.joa.alarm.contextfield.handlers;

import com.x.hhqs.joa.alarm.config.JoaAlarmConfig;
import com.x.hhqs.joa.alarm.contextfield.AlarmFields;
import com.x.hhqs.joa.alarm.dto.XAlarmContext;
import com.x.hhqs.joa.alarm.dto.XAlarmContextFields;
import com.x.hhqs.joa.alarm.utils.ChannelIndexUtil;
import org.springframework.stereotype.Component;

@Component
@DefaultHandler(field = AlarmFields.CHANNEL_INDEX)
public class ChannelIndexHandler extends AbstractAttributeHandler {

    private final JoaAlarmConfig joaAlarmConfig;

    public ChannelIndexHandler(JoaAlarmConfig joaAlarmConfig) {
        this.joaAlarmConfig = joaAlarmConfig;
    }

    @Override
    public String getId() {
        return AlarmFields.CHANNEL_INDEX;
    }

    @Override
    public String getValue(XAlarmContext context) {
        int channelIndex = 0;
        if (joaAlarmConfig.getServer().getChannelNumber() > 1) {
            if (!context.isDeviceAlarm()) {
                return String.valueOf(channelIndex);
            }
            String neIP = (String) context.getAttr(XAlarmContextFields.NE_IP);
            channelIndex = ChannelIndexUtil.getChannelIndex(neIP, joaAlarmConfig.getServer().getChannelNumber());
        }
        return String.valueOf(channelIndex);
    }
}
