package com.x.hhqs.joa.alarm.event;


import com.x.hhqs.joa.alarm.vo.OmcAlarmVO;
import lombok.*;

import java.util.List;

@Getter
@Setter
public class OmcAlarmEvent {

    private List<OmcAlarmVO> omcAlarmVOList;
}
