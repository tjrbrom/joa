package com.x.hhqs.joa.alarm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.x.hhqs.joa.alarm.entity.AlarmDefinitionInfo;
import com.x.hhqs.joa.alarm.exception.DependencyNotReadyException;
import com.x.hhqs.joa.alarm.service.AlarmDefinitionDaoService;
import com.x.hhqs.joa.alarm.service.AlarmDefinitionService;
import com.x.hhqs.joa.alarm.utils.POVOConverter;
import com.x.hhqs.joa.alarm.vo.AlarmDefinitionInfoVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AlarmDefinitionServiceImpl implements AlarmDefinitionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AlarmDefinitionServiceImpl.class);

    @Autowired
    private AlarmDefinitionDaoService alarmDefinitionDaoService;

    @Autowired
    private AlarmDefinitionInitializer alarmDefinitionInitializer;

    private void checkIfInitializingAlarmDefinitionsAndThrowException() {
        if (alarmDefinitionInitializer.isRunning()) {
            throw new DependencyNotReadyException("Alarm definition are initializing");
        }
    }

    @Override
    public List<AlarmDefinitionInfoVO> getAllAlarmDefinitions() {
        checkIfInitializingAlarmDefinitionsAndThrowException();
        List<AlarmDefinitionInfoVO> result = new ArrayList<>();
        List<AlarmDefinitionInfo> list = alarmDefinitionDaoService.list();
        if (list != null) {
            result = list.stream().map(POVOConverter::po2vo).toList();
        }
        return result;
    }

    @Override
    public AlarmDefinitionInfoVO getAlarmDefinitionByAlarmTypeId(String alarmTypeId) {
        checkIfInitializingAlarmDefinitionsAndThrowException();
        AlarmDefinitionInfo alarmDefinitionInfo = alarmDefinitionDaoService.getOne(new LambdaQueryWrapper<AlarmDefinitionInfo>()
                .eq(AlarmDefinitionInfo::getAlarmTypeId, alarmTypeId));
        if (alarmDefinitionInfo == null) {
            return null;
        }
        return POVOConverter.po2vo(alarmDefinitionInfo);
    }
}
