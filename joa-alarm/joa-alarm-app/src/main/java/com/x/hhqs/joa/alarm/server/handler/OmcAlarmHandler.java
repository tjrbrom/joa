package com.x.hhqs.joa.alarm.server.handler;

import cn.hutool.core.collection.CollectionUtil;
import com.x.hhqs.joa.alarm.logger.AlarmLogger;
import com.x.hhqs.joa.alarm.model.MessageType;
import com.x.hhqs.joa.alarm.model.OmcAlarmVOWrapper;
import com.x.hhqs.joa.alarm.vo.OmcAlarmVO;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class OmcAlarmHandler extends MessageToMessageEncoder<OmcAlarmVOWrapper> {

    public static final String NAME = "omcAlarmHandler";

    private final Charset charset;

    private final BlockingQueue<OmcAlarmVO> queue = new LinkedBlockingQueue<>();

    private long sendAlarmSeq = -1L;

    @Getter
    private long minSyncAlarmSeq = 0L;

    private String user;

    private int port;

    private volatile State state = State.NEW;

    private enum State {
        NEW, NORMAL, SYNC_ALARM;
    }

    private OmcAlarmHandler(String charsetName) {
        this.charset = StringUtils.isNotEmpty(charsetName) && Charset.isSupported(charsetName)
                ? Charset.forName(charsetName)
                : StandardCharsets.UTF_8;
    }

    public OmcAlarmHandler(String charsetName, String user, int port) {
        this(charsetName);
        this.user = user;
        this.port = port;
    }

    public void startSyncAlarm(long syncAlarmSeq) {
        this.state = State.SYNC_ALARM;
        this.minSyncAlarmSeq = syncAlarmSeq;
    }


    @Override
    protected void encode(ChannelHandlerContext ctx, OmcAlarmVOWrapper wrapper, List<Object> out) throws Exception {
        int localPort  = ((InetSocketAddress) ctx.channel().localAddress()).getPort();
        log.info("omc alarm handler {}", localPort);

        if (isWaitingForSyncAlarmResult()) {
            if (wrapper.isSyncAlarms()) {
                sendAlarmSeq = -1L;
                long lastSyncAlarmSeq = sendAlarmList(ctx, out, wrapper.getOmcAlarmVOs());
                long lastKeepAlarmSeq = sendKeepAlarms(ctx, out, lastSyncAlarmSeq);
                sendAlarmSeq = Math.max(lastSyncAlarmSeq, lastKeepAlarmSeq);
                log.info("last AlarmSeq after sync alarms:{}", sendAlarmSeq);
                finishSyncAlarm();
            } else {
                log.info("keepAlarms:{}", wrapper.isAlarmList());
                keepAlarms(wrapper);
            }
            return;
        }
        // report real alarms
        sendAlarmWrapper(ctx, wrapper, out);
    }

    /**
     * sync alarm state
     */
    public boolean isWaitingForSyncAlarmResult() {
        return state == State.SYNC_ALARM;
    }

    private long sendAlarmList(ChannelHandlerContext ctx, List<Object> out, List<OmcAlarmVO> alarms) {
        if (alarms.isEmpty())
            return -1L;

        for (OmcAlarmVO alarm : alarms) {
            sendAlarm(ctx, out, alarm);
        }

        return alarms.get(alarms.size() - 1).getAlarmSeq();
    }

    private void sendAlarm(ChannelHandlerContext ctx, List<Object> out, OmcAlarmVO alarm) {
        if (alarmSeqCanSend(alarm.getAlarmSeq())) {
            byte[] body = alarm.getJsonValue().getBytes(charset);

            ByteBuf buf = ctx.alloc().buffer();
            buf.writeShort(0xFFFF);
            buf.writeByte(MessageType.realTimeAlarm.getValue());
            buf.writeInt((int) (System.currentTimeMillis() / 1000));
            buf.writeShort(body.length);
            buf.writeBytes(body);

            out.add(buf);

            AlarmLogger.send(alarm, user, port, ctx.channel().remoteAddress());
        }
    }

    private boolean alarmSeqCanSend(long alarmSeq) {
        return alarmSeq > sendAlarmSeq;
    }

    private void keepAlarms(OmcAlarmVOWrapper wrapper) {
        if (wrapper.isAlarmList()) {
            queue.addAll(wrapper.getOmcAlarmVOs());
        } else {
            queue.add(wrapper.getOmcAlarmVO());
        }
    }

    private void finishSyncAlarm() {
        this.state = State.NORMAL;
        if (queue.isEmpty()) return;
        log.warn("SyncAlarm error, miss alarms to report. size = {}", queue.size());
    }

    private long sendKeepAlarms(ChannelHandlerContext ctx, List<Object> out, long lastAlarmSeq) {
        long lastKeepAlarmSeq = -1L;

        if (queue.isEmpty()) {
            return lastKeepAlarmSeq;
        }

        OmcAlarmVO alarm;
        while ((alarm = queue.poll()) != null) {
            if (alarm.getAlarmSeq() > lastAlarmSeq) {
                sendAlarm(ctx, out, alarm);
                lastKeepAlarmSeq = alarm.getAlarmSeq();
            }
        }

        return lastKeepAlarmSeq;
    }

    private void sendAlarmWrapper(ChannelHandlerContext ctx, OmcAlarmVOWrapper wrapper, List<Object> out) {
        if (wrapper.isAlarmList()) {
            sendAlarmList(ctx, out, wrapper.getOmcAlarmVOs());
        } else {
            sendAlarm(ctx, out, wrapper.getOmcAlarmVO());
        }
    }

}
