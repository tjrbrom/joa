package com.x.hhqs.joa.alarm.model.ams;

import com.x.hhqs.joa.alarm.entity.JoaAlarm;
import com.x.hhqs.joa.alarm.listener.kafka.processor.AsyncActiveAlarmMessageProcessor;
import com.x.hhqs.joa.alarm.model.OmcRequest;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Getter
@Setter
public class SyncAlarmFileRequest  {

    private String userName;

    private String reqSyncId;

    private String reqId;

    private long timestamp;

    private String syncSource ;

    private Channel channel;

    private OmcRequest omcRequest;

    private AmsMessageStatus status;

    private int channelIndex;

    private Boolean coreAlarmPrepared;

    private Boolean amsAlarmPrepared;

    private Boolean coreAlarmSucc;

    private List<String> amsFileNames;

    private String remoteIP;

    private Runnable completionListener;

    private final AtomicInteger state = new AtomicInteger(0);

    private static final int CORE_PREPARED = 1;

    private static final int AMS_PREPARED = 2;

    public SyncAlarmFileRequest(String userName,
                                String reqSyncId,
                                Channel channel,
                                OmcRequest omcRequest,
                                int startPort,
                                long timestamp) {
        this.userName = userName;
        this.reqId = reqSyncId + "_" + timestamp;
        this.reqSyncId = reqSyncId;
        this.channel = channel;
        this.omcRequest = omcRequest;
        this.syncSource = omcRequest.getValue("syncSource");
        this.status = AmsMessageStatus.request;
        this.channelIndex = getChannelIndex(getChannel(), startPort);
        this.timestamp = timestamp;
        this.coreAlarmPrepared = false;
        this.amsAlarmPrepared = false;
        this.coreAlarmSucc = false;
        this.amsFileNames = new ArrayList<>();
    }

    private int getChannelIndex(Channel ch, int startPort) {
        int port = ((InetSocketAddress) ch.localAddress()).getPort();
        return port - startPort;
    }

    public void setCompletionListener(Runnable listener) {
        this.completionListener = listener;
        checkAndTrigger();
    }

    public void markAmsAlarmPrepared(List<String> amsFileNames, String remoteIp) {
        this.remoteIP = remoteIp;
        this.amsFileNames = amsFileNames;
        amsAlarmPrepared = true;
        state.updateAndGet(s -> s | AMS_PREPARED);
        checkAndTrigger();
    }

    public void markCoreAlarmPrepared(Boolean coreAlarmSucc) {
        this.coreAlarmSucc = coreAlarmSucc;
        state.updateAndGet(s -> s | CORE_PREPARED);
        checkAndTrigger();
    }

    private synchronized void checkAndTrigger() {
        if (state.get() == (CORE_PREPARED | AMS_PREPARED) && completionListener != null) {
            triggerListener();
        }
    }

    private void triggerListener() {
        if (completionListener != null) {
            try {
                completionListener.run();
            } finally {
                completionListener = null;
                state.set(0);
            }
        }
    }
}
