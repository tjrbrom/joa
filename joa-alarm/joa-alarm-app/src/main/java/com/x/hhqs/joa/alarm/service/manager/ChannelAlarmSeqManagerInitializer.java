package com.x.hhqs.joa.alarm.service.manager;

import com.x.hhqs.joa.alarm.config.JoaAlarmConfig;
import com.x.hhqs.joa.alarm.entity.JoaAlarm;
import com.x.hhqs.joa.alarm.service.JoaAlarmDaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class ChannelAlarmSeqManagerInitializer {
    @Autowired
    private JoaAlarmDaoService joaAlarmDaoService;

    @Autowired
    private ChannelAlarmSeqManager channelAlarmSeqManager;

    @Autowired
    private JoaAlarmConfig joaAlarmConfig;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        Set<Integer> channelIndices = IntStream.range(0, joaAlarmConfig.getServer().getChannelNumber())
                .boxed()
                .collect(Collectors.toSet());
        Map<Integer, Long> maxSeqMap = new HashMap<>();
        for (JoaAlarm joaAlarm : joaAlarmDaoService.findMaxSeqAlarmByChannelIndices(channelIndices)) {
            maxSeqMap.put(joaAlarm.getChannelIndex(), joaAlarm.getAlarmSeq());
        }
        for (Integer channelIndex : channelIndices) {
            maxSeqMap.putIfAbsent(channelIndex, 0L);
        }
        channelAlarmSeqManager.initialize(maxSeqMap);
    }
}
