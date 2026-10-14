package com.x.hhqs.joa.common.restclient;

import com.x.hhqs.joa.common.restclient.metric.MetricManager;
import org.osgi.framework.*;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class HighAvailabilityClientRegister {
    private static final Logger LOGGER = LoggerFactory.getLogger(HighAvailabilityClientRegister.class);

    private static final String HA_EVENT_NAME = "com/x/high/availability/checker/EVENT";
    private static final String HA_EVENT_FILTER_TEMPLATE = "(&(type=ha)(application=%s))";

    private static BundleContext m_bundleContext;

    private static Hashtable<String, String> m_eventHandlerProperties;

    private static List<ServiceRegistration<EventHandler>> m_registeredEventHandlers;

    public HighAvailabilityClientRegister(BundleContext bundleContext) {
        m_bundleContext = bundleContext;
    }

    public void init() {
        if (m_registeredEventHandlers == null) {
            m_registeredEventHandlers = new ArrayList<>();
        }
        if (m_eventHandlerProperties == null) {
            m_eventHandlerProperties = new Hashtable<>();
            m_eventHandlerProperties.put(EventConstants.EVENT_TOPIC, HA_EVENT_NAME);
        }
    }

    public void destroy() {
        if (m_registeredEventHandlers != null) {
            m_registeredEventHandlers.forEach(registeredClient -> {
                if (registeredClient != null) {
                    registeredClient.unregister();
                }
            });
        }
    }

    public static ServiceRegistration<EventHandler> registerEventHandler(EventHandler client, String appName) {
        LOGGER.debug("Registering event handler for {}", appName);

        //for static call if bundle is not active
        if (m_eventHandlerProperties == null) {
            m_registeredEventHandlers = new ArrayList<>();
            m_eventHandlerProperties = new Hashtable<>();
            m_eventHandlerProperties.put(EventConstants.EVENT_TOPIC, HA_EVENT_NAME);
        }
        String eventFilter = String.format(HA_EVENT_FILTER_TEMPLATE, appName);
        m_eventHandlerProperties.put(EventConstants.EVENT_FILTER, eventFilter);
        ServiceRegistration<EventHandler> reference = null;

        //for static call if bundle is not active
        if (m_bundleContext != null) {
            reference = getEventHandlerServiceRegistration(client, m_bundleContext);
        } else {
            try {
                tryToGetBundleContext();
                if (m_bundleContext != null) {
                    reference = getEventHandlerServiceRegistration(client, m_bundleContext);
                } else {
                    LOGGER.error("BundleContext not found : {} " +
                                    "Failed to register event handler for {} ",
                            HighAvailabilityClientRegister.class.getSimpleName(), appName);
                }
            } catch (Exception e) {
                LOGGER.error("Failed to register event handler for {} ", appName, e);
            }
        }
        if (reference != null) {
            m_registeredEventHandlers.add(reference);
        }
        return reference;
    }

    public static void registerMetrics(Object metricContainer) {
        try {
            if (m_bundleContext != null) {
                proceedToRegisterMetrics(metricContainer);
            } else {
                tryToGetBundleContext();
                proceedToRegisterMetrics(metricContainer);
            }
        } catch (Exception e) {
            LOGGER.error("Error while registering metrics for " + metricContainer.getClass().getSimpleName(), e);
        }
    }

    private static ServiceRegistration<EventHandler> getEventHandlerServiceRegistration(EventHandler client, BundleContext bundleContext) {
        return bundleContext.registerService(EventHandler.class, client, m_eventHandlerProperties);
    }

    private static void proceedToRegisterMetrics(Object metricContainer) {
        if (m_bundleContext != null) {
            ServiceReference<MetricManager> metricManagerRef = m_bundleContext.getServiceReference(MetricManager.class);
            MetricManager metricManager = m_bundleContext.getService(metricManagerRef);
            metricManager.scanAndRegisterMetricicAnnotatedObject("rest.metric", metricContainer);
        } else {
            LOGGER.error("BundleContext not found : {} Error while registering metrics for {} ",
                    HighAvailabilityClientRegister.class.getSimpleName(), metricContainer.getClass().getSimpleName());
        }
    }

    private static void tryToGetBundleContext() {
        Bundle bundle = FrameworkUtil.getBundle(HighAvailabilityClientRegister.class);
        if (bundle != null) {
            m_bundleContext = bundle.getBundleContext();
        }
    }
}
