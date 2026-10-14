package com.x.hhqs.joa.alarm.model.ams;
import com.x.hhqs.joa.alarm.utils.ConnectKey;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class AmsAlarmMessage implements Serializable {

    private String reqId;

    private String startTime;

    private String endTime;

    private String userName;
}

