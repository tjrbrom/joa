package com.x.hhqs.joa.alarm.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;


import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActiveAlarmVO {

    @JsonProperty("raisedTime")
    private String raisedTime;

    @JsonProperty("objectId")
    private String objectId;

    @JsonProperty("alarmNotificationOrigin")
    private String alarmNotificationOrigin;

    @JsonProperty("lastStatusChangeTime")
    private String lastStatusChangeTime;

    @JsonProperty("alarmResource")
    private String alarmResource;

    private String alarmResourceUiName;

    private String deviceRefId;

    private String alarmTypeId;

    private String serviceAffecting;

    private String alarmType;

    private String alarmSeverity;

    private String alarmStatus;

    private String alarmText;

    private String proposedRepairAction;

    private String acknowledged;

    private String acknowledgementNote;

    private String PAP;

    private String hamObjectId;

    private String hamObjectFriendlyName;

    private String nrId;

    private String mainDeviceRefId;

    private List<String> labels;


}
