package com.x.hhqs.joa.alarm.handler.request;

import com.x.hhqs.joa.alarm.config.JoaAlarmConfig;
import com.x.hhqs.joa.alarm.entity.JoaAlarm;
import com.x.hhqs.joa.alarm.handler.RequestMessageHandler;
import com.x.hhqs.joa.alarm.logger.AlarmLogger;
import com.x.hhqs.joa.alarm.model.*;
import com.x.hhqs.joa.alarm.model.response.OmcAckMessage;
import com.x.hhqs.joa.alarm.property.Messages;
import com.x.hhqs.joa.alarm.server.ChannelUtil;
import com.x.hhqs.joa.alarm.server.handler.OmcAlarmHandler;
import com.x.hhqs.joa.alarm.service.AsyncTaskManager;
import com.x.hhqs.joa.alarm.service.OmcAlarmQueryService;
import com.x.hhqs.joa.alarm.vo.OmcAlarmVO;
import com.x.hhqs.joa.common.core.contants.MessageConstant;
import io.netty.channel.Channel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

@Component
public class ReqSyncAlarmMsgHandler extends RequestMessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(ReqSyncAlarmMsgHandler.class);

    @Autowired
    private AsyncTaskManager asyncTaskManager;

    @Autowired
    private JoaAlarmConfig joaAlarmConfig;

    @Resource
    private OmcAlarmQueryService omcAlarmQueryService;


    @Override
    public void executeRequest(Channel ch, OmcRequest request) {
        String reqId = request.getValue("reqId");

        ClientType clientType = ChannelUtil.getSession(ch).getClientType();
        // 1.check clientType is ClientType.msg
        if (clientType != ClientType.msg) {
            logger.error("ReqSyncAlarmMsg need execute with login type: msg.");
            fail(ch, reqId, Messages.getString("resDesc.wrongLoginType"));
            return;
        }

        // 2.check if channel is already waiting for ReqSyncAlarmMsg
        OmcAlarmHandler handler = (OmcAlarmHandler) ch.pipeline().get(OmcAlarmHandler.NAME);
        if (handler.isWaitingForSyncAlarmResult()) {
            logger.error("Last reqSyncAlarmMsg is not finished.");
            fail(ch, reqId, Messages.getString("resDesc.reqSyncAlarmMsgRunning"));
            return;
        }

        // 3. check parameters
        String alarmSeq = request.getValue("alarmSeq");
        if (!StringUtils.isNumeric(alarmSeq)) {
            fail(ch, reqId, invalidParamter("alarmSeq"));
            return;
        }

        if (Long.parseLong(alarmSeq) <= handler.getMinSyncAlarmSeq()) {
            fail(ch, reqId, MessageFormat.format(Messages.getString("resDesc.alarmSeqMustLarger"),
                    handler.getMinSyncAlarmSeq()));
            return;
        }

        // 4. do task
        long startSeq = Long.parseLong(alarmSeq);
        handler.startSyncAlarm(startSeq);
        asyncTaskManager.execute(createSyncAlarmMsgTask(reqId, startSeq, ch));
    }


    private void success(Channel ch, String reqId) {
        OmcAckMessage ackMessage = getOmcAckMessage();
        ackMessage.setValue("reqId", reqId);
        ackMessage.setValue("result", MessageConstant.SUCC);

        ch.writeAndFlush(ackMessage);
    }

    private void fail(Channel ch, String reqId, String resDesc) {
        OmcAckMessage ackMessage = getOmcAckMessage();
        ackMessage.setValue("reqId", reqId);
        ackMessage.setValue("result", MessageConstant.FAIL);
        ackMessage.setValue("resDesc", resDesc);

        ch.writeAndFlush(ackMessage);
    }

    private int getChannelIndex(Channel ch) {
        int port = ((InetSocketAddress) ch.localAddress()).getPort();
        return port - joaAlarmConfig.getServer().getPort();
    }

    SyncAlarmMsgTask createSyncAlarmMsgTask(String reqId, Long startSeq, Channel ch) {
        return new SyncAlarmMsgTask(reqId, startSeq, ch);
    }

    @Override
    public MessageType getType() {return MessageType.reqSyncAlarmMsg;}


    class SyncAlarmMsgTask implements Runnable {

        private final String reqId;

        private final Long alarmSeq;

        private final Channel ch;

        public SyncAlarmMsgTask(String reqId, Long alarmSeq, Channel ch) {
            this.reqId = reqId;
            this.alarmSeq = alarmSeq;
            this.ch = ch;
        }

        @Override
        public void run() {
            List<OmcAlarmVO> alarms = new ArrayList<>(0);
            try {
                int channelIndex = getChannelIndex(ch);
                // logger
                ClientSession session = ChannelUtil.getSession(ch);
                AlarmLogger.userSyncAlarmMsg(session.getUserName(), ch.remoteAddress(), reqId, alarmSeq);

                JoaAlarm alarm = omcAlarmQueryService.getOmcAlarmByAlarmSeq(alarmSeq, channelIndex);
                if (alarm == null) {
                    logger.warn("alarmSeq doesn't exist and ignore ReqSyncAlarmMsg, alarmSeq={}", alarmSeq);
                    // if alarm with alarmSeq does not exist, ignore this sync
                    fail(ch, reqId, Messages.getString("resDesc.alarmSeqNotExist"));
                } else {
                    Integer counts = omcAlarmQueryService.countAlarmsStartWithId(alarm.getId(), channelIndex);
                    if (counts > 1000) {
                        alarms = new ArrayList<>(0);
                        fail(ch, reqId, "Alarms more than 1000.");
                    } else {
                        alarms = omcAlarmQueryService.getAlarmsStartWithId(alarm.getId(), channelIndex);
                        success(ch, reqId);
                        logger.info("ReqSyncAlarmMsg finished, alarmSeq={}", alarmSeq);
                    }
                }
            } catch (Exception e) {
                logger.error("", e);
                // exception occurs, notify client
                fail(ch, reqId, Messages.getString("resDesc.executeFail"));
            } finally {
                // send sync result to client
                // an empty list is needed to stop sync if exception occurs or
                // alarmSeq does not exist
                ch.writeAndFlush(OmcAlarmVOWrapper.wrapperSyncAlarms(alarms));
            }
        }

    }
}
