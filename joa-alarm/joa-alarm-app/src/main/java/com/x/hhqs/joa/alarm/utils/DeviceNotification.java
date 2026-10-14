package com.x.hhqs.joa.alarm.utils;

import com.x.hhqs.joa.alarm.dto.XAlarmVO;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.StringJoiner;

@XmlRootElement(name = "notification")
@XmlAccessorType(XmlAccessType.FIELD)
public class DeviceNotification {

    @XmlElement(name = "eventTime")
    private String eventTime;

    @XmlElement(name = "alarm-notification")
    private DeviceAlarmNotification deviceAlarmNotification;

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    public DeviceAlarmNotification getDeviceAlarmNotification() {
        return deviceAlarmNotification;
    }

    public void setDeviceAlarmNotification(DeviceAlarmNotification deviceAlarmNotification) {
        this.deviceAlarmNotification = deviceAlarmNotification;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", DeviceNotification.class.getSimpleName() + "[", "]")
                .add("eventTime='" + eventTime + "'")
                .add("deviceAlarmNotification=" + deviceAlarmNotification)
                .toString();
    }

    public XAlarmVO getXAlarmVO() {
        return deviceAlarmNotification.getXAlarmVO();
    }
}
