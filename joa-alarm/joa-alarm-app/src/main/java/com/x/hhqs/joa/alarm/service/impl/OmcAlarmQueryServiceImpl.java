package com.x.hhqs.joa.alarm.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.x.hhqs.joa.alarm.config.JoaAlarmConfig;
import com.x.hhqs.joa.alarm.entity.JoaAlarm;
import com.x.hhqs.joa.alarm.mapper.JoaAlarmMapper;
import com.x.hhqs.joa.alarm.model.OmcRequest;
import com.x.hhqs.joa.alarm.service.JoaAlarmDaoService;
import com.x.hhqs.joa.alarm.service.FileUploadService;
import com.x.hhqs.joa.alarm.service.OmcAlarmQueryService;
import com.x.hhqs.joa.alarm.utils.SyncAlarmFile;
import com.x.hhqs.joa.alarm.utils.SyncAlarmFileUtil;
import com.x.hhqs.joa.alarm.vo.OmcAlarmVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cursor.Cursor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.x.hhqs.joa.alarm.noncodedplugs.utils.SoftwareExtensionUtil.NEW_LINE;

@Service
@Slf4j
public class OmcAlarmQueryServiceImpl implements OmcAlarmQueryService {

    @Resource
    private JoaAlarmDaoService joaAlarmDaoService;

    @Override
    public JoaAlarm getOmcAlarmByAlarmSeq(Long alarmSeq, int channelIndex) {
        QueryWrapper<JoaAlarm> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("alarm_seq", alarmSeq)
                .eq("channel_index", channelIndex);
        return this.joaAlarmDaoService.getOne(queryWrapper);
    }

    @Override
    public Integer countAlarmsStartWithId(Long id, int channelIndex) {
        QueryWrapper<JoaAlarm> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("channel_index", channelIndex)
                .ge("id", id);
        return this.joaAlarmDaoService.count(queryWrapper);
    }

    @Override
    public List<OmcAlarmVO> getAlarmsStartWithId(Long id, int channelIndex) {
        List<OmcAlarmVO> result = new ArrayList<>();
        QueryWrapper<JoaAlarm> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("channel_index", channelIndex)
                .ge("id", id);
        List<JoaAlarm> alarms = this.joaAlarmDaoService.list(queryWrapper);
        for (JoaAlarm alarm : alarms) {
            result.add(makeOmcAlarmVO(alarm));
        }
        return result;
    }

    @Override
    public OmcAlarmVO makeOmcAlarmVO(JoaAlarm alarm){
        return OmcAlarmVO.create(alarm.getId(), alarm.getAlarmSeq(), alarm.getCreateTime(),
                JSONUtil.toJsonStr(alarm.getJsonValue()),
                alarm.getChannelIndex(), alarm.getAlarmId(), alarm.getNeName());
    }

    @Override
    public List<OmcAlarmVO> getAlarmsDuringTime(Date startTime, Date endTime, int channelIndex) {
        QueryWrapper<JoaAlarm> joaAlarmQueryWrapper = new QueryWrapper<>();
        joaAlarmQueryWrapper.eq("channel_index", channelIndex);
        joaAlarmQueryWrapper.ge(startTime != null, "create_time", startTime);
        joaAlarmQueryWrapper.le("create_time",endTime == null ? DateTime.now() : endTime);
        return this.joaAlarmDaoService.list(joaAlarmQueryWrapper)
                .stream().map(this::makeOmcAlarmVO).toList();
    }

    /**
     *
     * reqSyncAlarmFile;reqId=33;startTime=2014-11-27 10:00:00;endTime=2014-11-27 10:30:00; syncSource =0
     * reqSyncAlarmFile;reqId=35;alarmSeq=100;syncSource =0
     * @param request
     * @param channelIndex
     * @return
     */
    @Override
    public List<OmcAlarmVO> queryCurrentAlarms(OmcRequest request, int channelIndex) {
        return List.of();
    }
}
