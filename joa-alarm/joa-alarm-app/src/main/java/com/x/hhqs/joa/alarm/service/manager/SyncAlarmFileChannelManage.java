package com.x.hhqs.joa.alarm.service.manager;

import com.x.hhqs.joa.alarm.config.JoaAlarmConfig;
import com.x.hhqs.joa.alarm.listener.kafka.processor.ActiveAlarmProcessor;
import com.x.hhqs.joa.alarm.model.ClientSession;
import com.x.hhqs.joa.alarm.model.ams.AmsMessageStatus;
import com.x.hhqs.joa.alarm.model.ams.SyncAlarmFileRequest;
import com.x.hhqs.joa.alarm.server.ChannelUtil;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

@Component
@Slf4j
public class SyncAlarmFileChannelManage {

    private final ConcurrentHashMap<String, SyncAlarmFileRequest> activeRequests = new ConcurrentHashMap<>();

    public void saveRequestToStorage(String reqId, SyncAlarmFileRequest request) {
        activeRequests.compute(reqId, (k, v) -> {
            if (v != null) {
                log.debug("Updated existing request: reqId={}", reqId);
            }
            return request;
        });
    }

    public void removeRequestFromStorage(String reqId) {
        log.info("Removing request: reqSyncId={}", reqId);
        activeRequests.remove(reqId);
    }

    public SyncAlarmFileRequest getSyncAlarmFileRequest(String reqSyncId) {
        return activeRequests.get(reqSyncId);
    }

    public boolean isExistSyncAlarmFileRequest(String userName, int port) {
        String reqId = String.format("%s_%s", userName, port);
        return activeRequests.searchKeys(1, key -> key.equals(reqId) ? key : null) != null;
    }


}
