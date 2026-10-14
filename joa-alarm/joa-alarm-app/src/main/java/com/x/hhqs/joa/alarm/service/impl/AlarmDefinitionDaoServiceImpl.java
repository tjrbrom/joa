package com.x.hhqs.joa.alarm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.x.hhqs.joa.alarm.entity.AlarmDefinitionInfo;
import com.x.hhqs.joa.alarm.mapper.AlarmDefinitionMapper;
import com.x.hhqs.joa.alarm.service.AlarmDefinitionDaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlarmDefinitionDaoServiceImpl extends ServiceImpl<AlarmDefinitionMapper, AlarmDefinitionInfo> implements AlarmDefinitionDaoService {
    @Autowired
    AlarmDefinitionMapper alarmDefinitionMapper;

    @Override
    @Transactional(rollbackFor = {RuntimeException.class})
    public void truncateTable() {
        alarmDefinitionMapper.truncateTable();
    }
}
