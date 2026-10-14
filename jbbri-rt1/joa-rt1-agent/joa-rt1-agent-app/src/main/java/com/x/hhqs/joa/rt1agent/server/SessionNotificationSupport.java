package com.x.hhqs.joa.rt1agent.server;
//package com.nsb.jbbri.rt1agent.server;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Component;
//
//import com.asb.ponnbi.api.authentication.notification.SessionListener;
//
//@Component
//public class SessionNotificationSupport {
//	private static final Logger LOGGER = LoggerFactory.getLogger(SessionNotificationSupport.class);
//
//	private List<SessionListener> listeners = new ArrayList<>();
//
//	public void addListener(SessionListener listener) {
//		synchronized (listeners) {
//			if (!listeners.contains(listener)) {
//				listeners.add(listener);
//			}
//		}
//	}
//
//	public void removeListener(SessionListener listener) {
//		synchronized (listeners) {
//			listeners.remove(listener);
//		}
//	}
//
//	private List<SessionListener> getSafeListeners() {
//		synchronized (listeners) {
//			return new ArrayList<>(listeners);
//		}
//	}
//
//	public void fireSessionActive(String sessionId) {
//		for (SessionListener listener : getSafeListeners()) {
//			try {
//				listener.sessionActive(sessionId);
//			} catch (Exception e) {
//				LOGGER.error(e, e);
//			}
//		}
//	}
//
//	public void fireSessionInactive(String sessionId) {
//		for (SessionListener listener : getSafeListeners()) {
//			try {
//				listener.sessionInactive(sessionId);
//			} catch (Exception e) {
//				LOGGER.error(e, e);
//			}
//		}
//	}
//}
