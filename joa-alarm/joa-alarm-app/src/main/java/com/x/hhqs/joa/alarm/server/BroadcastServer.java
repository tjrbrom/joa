package com.x.hhqs.joa.alarm.server;

import java.util.List;


import com.x.hhqs.joa.alarm.listener.AlarmReceiver;
import com.x.hhqs.joa.alarm.listener.InnerAlarmListener;
import com.x.hhqs.joa.alarm.model.ClientSession;
import com.x.hhqs.joa.alarm.model.OmcAlarmVOWrapper;
import com.x.hhqs.joa.alarm.vo.OmcAlarmVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

@Service
@Slf4j
public class BroadcastServer implements InnerAlarmListener, InitializingBean, DisposableBean {

    @Autowired
    private AlarmReceiver alarmReceiver;

    private ChannelGroup recipients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    public void afterPropertiesSet() {
        alarmReceiver.addListener(this);
    }

    @Override
    public void destroy() {
        alarmReceiver.removeListener(this);
    }

    public void addChannel(Channel ch) {
        log.info("BroadcastServer add channel : {}", ch.remoteAddress().toString());
        recipients.add(ch);
    }

    public void removeChannel(Channel ch) {
        log.info("channel disconnected: {}", ch);
        recipients.remove(ch);
    }

    public void boadrcast(OmcAlarmVOWrapper wrapper) {
        log.info("connected sessions size: {}", recipients.size());
        if (recipients.isEmpty()) return;
        for (Channel ch : recipients) {
            ClientSession session = ChannelUtil.getSession(ch);
            log.info("session: {}, {}, {}", session.getUserName(), session.getSessionId(), ch);
        }
        recipients.writeAndFlush(wrapper);
        log.info("Send to channels end");
    }

    @Override
    public void alarmsReceived(List<OmcAlarmVO> alarms) {
        log.info("received from topic_oas_omcAlarm: {}", alarms.size());
        boadrcast(new OmcAlarmVOWrapper(alarms));
    }

}
