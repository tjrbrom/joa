package com.x.hhqs.joa.alarm.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "alarm")
@XmlAccessorType(XmlAccessType.FIELD)
public class XAlarmVO {
    @XmlElement(name = "PAP")
    private String alarmPap;

    @XmlElement(name = "alarm-id")
    private String alarmId;

    @XmlElement(name = "alarmNotificationOrigin")
    private String alarmNotificationOrigin;

    @XmlElement(name = "alarmResource")
    private String alarmResource;

    @XmlElement(name = "alarmResourceUiName")
    private String alarmResourceUiName;

    @XmlElement(name = "alarmSeverity")
    private String alarmSeverity;

    @XmlElement(name = "alarmStatus")
    private String alarmStatus;

    @XmlElement(name = "alarmText")
    private String alarmText;

    @XmlElement(name = "alarmType")
    private String alarmType;

    @XmlElement(name = "alarmTypeId")
    private String alarmTypeId;

    @XmlElement(name = "lastStatusChangeTime")
    private String lastStatusChangeTime;

    @XmlElement(name = "objectId")
    private String objectId;

    @XmlElement(name = "proposedRepairAction")
    private String proposedRepairAction;

    @XmlElement(name = "raisedTime")
    private String raisedTime;

    @XmlElement(name = "clearedTime")
    private String clearedTime;

    public String getAlarmPap() {
        return alarmPap;
    }

    public void setAlarmPap(String alarmPap) {
        this.alarmPap = alarmPap;
    }

    public String getAlarmId() {
        return alarmId;
    }

    public void setAlarmId(String alarmId) {
        this.alarmId = alarmId;
    }

    public String getAlarmNotificationOrigin() {
        return alarmNotificationOrigin;
    }

    public void setAlarmNotificationOrigin(String alarmNotificationOrigin) {
        this.alarmNotificationOrigin = alarmNotificationOrigin;
    }

    public String getAlarmResource() {
        return alarmResource;
    }

    public void setAlarmResource(String alarmResource) {
        this.alarmResource = alarmResource;
    }

    public String getAlarmResourceUiName() {
        return alarmResourceUiName;
    }

    public void setAlarmResourceUiName(String alarmResourceUiName) {
        this.alarmResourceUiName = alarmResourceUiName;
    }

    public String getAlarmSeverity() {
        return alarmSeverity;
    }

    public void setAlarmSeverity(String alarmSeverity) {
        this.alarmSeverity = alarmSeverity;
    }

    public String getAlarmStatus() {
        return alarmStatus;
    }

    public void setAlarmStatus(String alarmStatus) {
        this.alarmStatus = alarmStatus;
    }

    public String getAlarmText() {
        return alarmText;
    }

    public void setAlarmText(String alarmText) {
        this.alarmText = alarmText;
    }

    public String getAlarmType() {
        return alarmType;
    }

    public void setAlarmType(String alarmType) {
        this.alarmType = alarmType;
    }

    public String getAlarmTypeId() {
        return alarmTypeId;
    }

    public void setAlarmTypeId(String alarmTypeId) {
        this.alarmTypeId = alarmTypeId;
    }

    public String getLastStatusChangeTime() {
        return lastStatusChangeTime;
    }

    public void setLastStatusChangeTime(String lastStatusChangeTime) {
        this.lastStatusChangeTime = lastStatusChangeTime;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getProposedRepairAction() {
        return proposedRepairAction;
    }

    public void setProposedRepairAction(String proposedRepairAction) {
        this.proposedRepairAction = proposedRepairAction;
    }

    public String getRaisedTime() {
        return raisedTime;
    }

    public void setRaisedTime(String raisedTime) {
        this.raisedTime = raisedTime;
    }

    public String getClearedTime() {
        return clearedTime;
    }

    public void setClearedTime(String clearedTime) {
        this.clearedTime = clearedTime;
    }

    @Override
    public String toString() {
        return "XAlarmVO{" +
                "alarmPap='" + alarmPap + '\'' +
                ", alarmId='" + alarmId + '\'' +
                ", alarmNotificationOrigin='" + alarmNotificationOrigin + '\'' +
                ", alarmResource='" + alarmResource + '\'' +
                ", alarmResourceUiName='" + alarmResourceUiName + '\'' +
                ", alarmSeverity='" + alarmSeverity + '\'' +
                ", alarmStatus='" + alarmStatus + '\'' +
                ", alarmText='" + alarmText + '\'' +
                ", alarmType='" + alarmType + '\'' +
                ", alarmTypeId='" + alarmTypeId + '\'' +
                ", lastStatusChangeTime='" + lastStatusChangeTime + '\'' +
                ", objectId='" + objectId + '\'' +
                ", proposedRepairAction='" + proposedRepairAction + '\'' +
                ", raisedTime='" + raisedTime + '\'' +
                ", clearedTime='" + clearedTime + '\'' +
                '}';
    }
}
