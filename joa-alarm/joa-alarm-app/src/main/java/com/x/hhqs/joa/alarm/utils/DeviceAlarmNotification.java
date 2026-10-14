package com.x.hhqs.joa.alarm.utils;

import com.x.hhqs.joa.alarm.dto.XAlarmVO;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.StringJoiner;

@XmlRootElement(name = "alarm-notification")
@XmlAccessorType(XmlAccessType.FIELD)
public class DeviceAlarmNotification {

    @XmlElement(name = "alarm")
    private XAlarmVO xAlarmVO;

    public XAlarmVO getXAlarmVO() {
        return xAlarmVO;
    }

    public void setAlarmVO(XAlarmVO xAlarmVO) {
        this.xAlarmVO = xAlarmVO;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", DeviceAlarmNotification.class.getSimpleName() + "[", "]")
                .add("xAlarmVO=" + xAlarmVO)
                .toString();
    }
}