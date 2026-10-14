package com.x.hhqs.joa.alarm.schedule;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.x.hhqs.joa.alarm.config.JoaAlarmConfig;
import com.x.hhqs.joa.alarm.entity.JoaAlarm;
import com.x.hhqs.joa.alarm.service.JoaAlarmDaoService;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ClearHistoryAlarmSchedule {

    @Resource
    private JoaAlarmDaoService joaAlarmDaoService;

    @Resource
    private JoaAlarmConfig joaAlarmConfig;

//    @Scheduled(cron = "0 */5 * * * *")
    @Scheduled(cron = "0 20 1 * * ?")
    public void clearHistoryAlarmSchedule() {
        log.info("clear DB periodically according, retain data for {} days", joaAlarmConfig.getDatabaseKeepDays());
        LocalDateTime dateTime = LocalDate.now().atStartOfDay().minusDays(joaAlarmConfig.getDatabaseKeepDays());
        QueryWrapper<JoaAlarm> queryWrapper = new QueryWrapper<>();
        queryWrapper.lt("create_time", dateTime);
        int count = joaAlarmDaoService.count(queryWrapper);
        if (count > 0) {
            joaAlarmDaoService.remove(queryWrapper);
        }
        log.info("clean DB end, total {} ", count);
    }
}
