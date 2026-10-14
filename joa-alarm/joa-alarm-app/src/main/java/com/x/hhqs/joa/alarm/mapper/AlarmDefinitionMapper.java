package com.x.hhqs.joa.alarm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.x.hhqs.joa.alarm.entity.AlarmDefinitionInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AlarmDefinitionMapper extends BaseMapper<AlarmDefinitionInfo> {
    @Update("TRUNCATE TABLE AlarmDefinition")
    void truncateTable();
}
