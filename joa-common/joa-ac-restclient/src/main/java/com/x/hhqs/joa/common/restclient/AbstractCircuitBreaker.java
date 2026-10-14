package com.x.hhqs.joa.common.restclient;

import com.x.hhqs.joa.common.restclient.ha.HighAvailabilityConnectionState;
import com.x.hhqs.joa.common.restclient.ha.HighAvailabilityState;
import com.x.hhqs.joa.common.restclient.ha.HighAvailabilityStateChangeListener;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class AbstractCircuitBreaker implements EventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCircuitBreaker.class);

    protected static final String CB_CONNECTION_STATUS_METRIC = "circuit.breaker.connection.status";

    protected AtomicInteger m_connectionStatusMetric = new AtomicInteger(1);

    protected AtomicLong m_connectionLossFailuresMetric = new AtomicLong(0);

    protected AtomicBoolean m_isCircuitOpen = new AtomicBoolean(false);

    private Set<HighAvailabilityStateChangeListener> m_haStateChangeListeners = new HashSet<>();

    private HighAvailabilityState m_haState = HighAvailabilityState.DISCONNECTED;

    private String m_name;

    public AbstractCircuitBreaker(String name) {
        m_name = name;
    }

    ServiceRegistration<EventHandler> registerForHAEvents(BundleContext bundleContext, String appNameFilter) {
        ServiceRegistration<EventHandler> registration = null;
        return registration;
    }

    public synchronized void addHighAvailabilityStateChangeListener(HighAvailabilityStateChangeListener listener){
        m_haStateChangeListeners.add(listener);
    }

    public synchronized void removeHighAvailabilityStateChangeListener(HighAvailabilityStateChangeListener listener){
        m_haStateChangeListeners.remove(listener);
    }

    private Set<HighAvailabilityStateChangeListener> getHighAvailabilityStateChangeListeners(){
        return m_haStateChangeListeners;
    }

    private HighAvailabilityState getHighAvailabilityState() {
        return m_haState;
    }

    protected void setHighAvailabilityState(HighAvailabilityState eSstate) {
        this.m_haState = eSstate;
    }

    protected void notifyListenersOnHAStateChange(){
        HighAvailabilityState oldState = getHighAvailabilityState();
        HighAvailabilityState currentState = isConnected() ? HighAvailabilityState.CONNECTED : HighAvailabilityState.DISCONNECTED;
        if(currentState != oldState) {
            setHighAvailabilityState(currentState);
            Set<HighAvailabilityStateChangeListener> listeners = getHighAvailabilityStateChangeListeners();
            for(HighAvailabilityStateChangeListener listener : listeners) {
                try {
                    listener.onStateChange(currentState);
                } catch (Exception e){
                    LOGGER.warn("Error notifying listener of " + m_name + " circuit breaker. Ignoring", e);
                }
            }
        }
    }

    protected void incrementFailureCount() {
        m_connectionLossFailuresMetric.incrementAndGet();
    }

    /**
     * By default the circuit will trip on IO Exceptions. The assumptions is
     * that an IOException indicates that there is a connectivity issue. If this
     * is not the case in your particular circuit breaker, this method should be
     * overridden. (Ex: HHQS-57431 Webdav CB needs to override shouldTripCircuit)
     *
     */
    public boolean shouldTripCircuit(Throwable e) {
        while(e != null){
            if (e instanceof IOException) {
                LOGGER.debug("Tripping circuit breaker due to exception - " + e.getMessage(), e);
                return true;
            }
            e = e.getCause();
        }
        return false;
    }

    public boolean isConnected(){
        return !(m_isCircuitOpen.get());
    }

    public boolean isDisconnected(){
        return (m_isCircuitOpen.get());
    }

    public void tripCircuit() {
        LOGGER.error(m_name + " circuit breaker has tripped!");
        m_isCircuitOpen.set(true);
        m_connectionStatusMetric.set(0);
        notifyListenersOnHAStateChange();
    }

    public void tripCircuit(Throwable cause) {
        LOGGER.error(m_name + " circuit breaker has tripped!", cause);
        m_isCircuitOpen.set(true);
        m_connectionStatusMetric.set(0);
        notifyListenersOnHAStateChange();
    }

    public void closeCircuit() {
        LOGGER.info(m_name + " circuit breaker has closed (Connectivity restored)");
        m_isCircuitOpen.set(false);
        m_connectionStatusMetric.set(1);
        m_connectionLossFailuresMetric.set(0);
        notifyListenersOnHAStateChange();
    }


    @Override
    public void handleEvent(Event event) {
        HighAvailabilityConnectionState connectionState = (HighAvailabilityConnectionState) event.getProperty("connectionState");
        boolean nowDisconnected = HighAvailabilityConnectionState.DEAD.equals(connectionState);
        boolean nowConnected = !nowDisconnected;

        if(isConnected() && nowDisconnected) {
            tripCircuit();
        } else if(isDisconnected() && nowConnected) {
            closeCircuit();
        }
    }

    protected int getCircuitBreakerConnectionStatus(){
        return m_connectionStatusMetric.get();
    }

    protected long getFailureCountDueToCircuitOpen(){
        return m_connectionLossFailuresMetric.get();
    }
}
