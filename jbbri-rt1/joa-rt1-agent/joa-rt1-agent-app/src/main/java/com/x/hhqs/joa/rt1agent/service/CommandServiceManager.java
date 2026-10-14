package com.x.hhqs.joa.rt1agent.service;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class CommandServiceManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandServiceManager.class);
    @Autowired
    private NacosServiceManager serviceManager;

    @Autowired
    private NacosDiscoveryProperties properties;

    @Value("${joa.rt1agent.cloud.subscribed-services}")
    private String subscribedServices;

    public void setSubscribedServices(String subscribedServices) {
        this.subscribedServices = subscribedServices;
    }

    private static final Map<String, List<Instance>> instanceMap = new ConcurrentHashMap<>();

    public List<Instance> getAllInstances() {
        if (instanceMap.isEmpty()) {
            return new ArrayList<>();
        } else {
            return instanceMap.values().stream().flatMap(List::stream).collect(Collectors.toList());
        }
    }

    @PostConstruct
    public void init() {
        try {
            NamingService namingService = serviceManager.getNamingService(properties.getNacosProperties());
            if (subscribedServices != null) {
                String[] services = subscribedServices.split(",");
                for (String service : services) {
                    String trimService = service.trim();
                    namingService.subscribe(trimService, new CommandServiceListener());
                    LOGGER.info("Subscribed service {}", trimService);
                }
            }
        } catch (NacosException e) {
            LOGGER.error("Error occurred while subscribing services.", e);
        }
    }

    @PreDestroy
    public void destroy() {
        try {
            if (subscribedServices != null) {
                NamingService namingService = serviceManager.getNamingService(properties.getNacosProperties());
                String[] services = subscribedServices.split(",");
                for (String service : services) {
                    String trimService = service.trim();
                    namingService.unsubscribe(trimService, new CommandServiceListener());
                    LOGGER.info("Unsubscribed service {}", trimService);
                }
            }
        } catch (NacosException e) {
            LOGGER.error("Error occurred while unsubscribing services.", e);
        }
    }

    private static class CommandServiceListener implements EventListener {
        @Override
        public void onEvent(Event event) {
            NamingEvent namingEvent = (NamingEvent) event;
            String serviceName = namingEvent.getServiceName();
            List<Instance> instances = namingEvent.getInstances();
            LOGGER.info("Event from service {}, instances {}.", serviceName, instances);
            instanceMap.put(serviceName, instances);
        }
    }

}
