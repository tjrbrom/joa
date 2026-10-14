package com.x.hhqs.joa.alarm.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.x.hhqs.joa.alarm.entity.JoaAlarm;
import com.x.hhqs.joa.alarm.vo.OmcAlarmVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.mapping.ResultSetType;
import org.apache.ibatis.session.SqlSession;

import java.util.List;
import java.util.Set;

@Mapper
public interface JoaAlarmMapper extends BaseMapper<JoaAlarm> {
    /**
     * Get max id per channel.
     */
    List<Long> findMaxIdByChannelIndices(@Param("channelIndices") Set<Integer> channelIndices);
    /**
     * Get all joa_alarms with max ids.
     */
    List<JoaAlarm> findAlarmsByIds(@Param("ids") List<Long> ids);

    @Select("SELECT id,alarm_seq as alarmSeq," +
            "create_time as createTime, " +
            "json_value as jsonValue, channel_index as channelIndex, " +
            "alarm_id as alarmId, ne_name as neName" +
            "  FROM joa_alarm ${ew.customSqlSegment}")
    @Options(resultSetType = ResultSetType.FORWARD_ONLY,fetchSize = 1000)
    Cursor<OmcAlarmVO> selectCursor(@Param("ew") QueryWrapper<JoaAlarm> wrapper);

}
