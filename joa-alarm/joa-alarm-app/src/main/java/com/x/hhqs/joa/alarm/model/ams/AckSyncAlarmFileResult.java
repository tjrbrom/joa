package com.x.hhqs.joa.alarm.model.ams;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AckSyncAlarmFileResult {

    private AmsMessageStatus status;

    private String serverIp;

    private List<String> fileNames = new ArrayList<>();


}
