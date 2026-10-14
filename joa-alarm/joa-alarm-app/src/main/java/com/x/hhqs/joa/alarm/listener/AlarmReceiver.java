package com.x.hhqs.joa.alarm.listener;

import com.google.common.eventbus.Subscribe;

import com.x.hhqs.joa.alarm.event.IEventHandler;
import com.x.hhqs.joa.alarm.event.OmcAlarmEvent;
import com.x.hhqs.joa.alarm.logger.SeqAlarmLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class AlarmReceiver implements IEventHandler {

	private final List<InnerAlarmListener> alarmListeners = new CopyOnWriteArrayList<>();

	public AlarmReceiver() {
	}

	public synchronized void addListener(InnerAlarmListener listener) {
		if (!alarmListeners.contains(listener))
			alarmListeners.add(listener);
	}

	public synchronized void removeListener(InnerAlarmListener listener) {
		alarmListeners.remove(listener);
	}

	@Subscribe
	public void onMessage(OmcAlarmEvent message) {
		log.info("------alarmReceived:---------");
		SeqAlarmLogger.send(message.getOmcAlarmVOList());
		if (alarmListeners.isEmpty()) {
			log.warn("No alarm listeners registered!");
			return;
		}
		for (InnerAlarmListener listener : alarmListeners) {
			listener.alarmsReceived(message.getOmcAlarmVOList());
		}
	}
}
