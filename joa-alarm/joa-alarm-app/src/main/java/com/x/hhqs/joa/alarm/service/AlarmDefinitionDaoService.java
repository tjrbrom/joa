package com.x.hhqs.joa.alarm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.x.hhqs.joa.alarm.entity.AlarmDefinitionInfo;

public interface AlarmDefinitionDaoService extends IService<AlarmDefinitionInfo> {
    public void truncateTable();
}
