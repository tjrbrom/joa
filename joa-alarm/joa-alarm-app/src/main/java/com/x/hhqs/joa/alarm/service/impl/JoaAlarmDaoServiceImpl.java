package com.x.hhqs.joa.alarm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.x.hhqs.joa.alarm.entity.JoaAlarm;
import com.x.hhqs.joa.alarm.mapper.JoaAlarmMapper;
import com.x.hhqs.joa.alarm.service.JoaAlarmDaoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
public class JoaAlarmDaoServiceImpl extends ServiceImpl<JoaAlarmMapper, JoaAlarm> implements JoaAlarmDaoService {

    @Autowired
    private JoaAlarmMapper joaAlarmMapper;

    @Override
    public List<JoaAlarm> findMaxSeqAlarmByChannelIndices(Set<Integer> channel_indices) {
        List<Long> maxIds = joaAlarmMapper.findMaxIdByChannelIndices(channel_indices);
        if (maxIds.isEmpty()) {
            return Collections.emptyList();
        }
        return joaAlarmMapper.findAlarmsByIds(maxIds);
    }

    @Override
    public List<JoaAlarm> findAllAlarmsByIds(List<String> alarmIds) {
        LambdaQueryWrapper<JoaAlarm> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(JoaAlarm::getAlarmId, alarmIds);
        return this.list(queryWrapper);
    }
}
