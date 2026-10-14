package com.x.hhqs.joa.mds.sync;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.x.hhqs.joa.mds.emsclient.ac.MdsRestClient;
import com.x.hhqs.joa.mds.model.Device;
import com.x.hhqs.joa.mds.model.Manager;
import com.x.hhqs.joa.mds.model.ManagerNode;
import com.x.hhqs.joa.mds.model.ManagerType;

@Service
public class MdsContextService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MdsContextService.class);

    private Map<String, ManagerNode> managerNodeCache = new HashMap<>();

    @Autowired
    private SyncTaskConfig taskConfig;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MdsRestClient mdsRestClient;

    @Autowired
    private AmsSyncService amsSyncService;

    @Autowired
    private NavSyncService navSyncService;

    private ThreadPoolTaskScheduler scheduler;

    private ScheduledFuture<?> scheduledFuture;

    @PostConstruct
    public void initTaskScheduler() {
        this.scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("SyncScheduler-");
        this.scheduler.initialize();
    }

    @EventListener(ApplicationPreparedEvent.class)
    public void onApplicationReady() {
        scheduleTask(taskConfig.getIntervalMinutes());
    }

    public void scheduleTask(long minutes) {
        LOGGER.info("Schedule the synchronization task at a fixed interval of {} minute.", minutes);

        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }
        scheduledFuture = scheduler.scheduleWithFixedDelay(this::syncMdsContext,
            Duration.ofMinutes(minutes));
    }

    public void syncMdsContext() {
        if (!taskConfig.isEnabled()) {
            LOGGER.warn("Property 'joa.mds.sync-task.enabled' is false. Skipping synchronization task.");
            return;
        }

        if (taskConfig.getManagerTypes() == null || taskConfig.getManagerTypes().isEmpty()) {
            managerNodeCache.clear();
            LOGGER.warn("Property 'joa.mds.sync-task.manager-types' is empty. Skipping synchronization task and clear datas.");
            return;
        }

        try {
            LOGGER.info("Starting synchronization of all managers.");
            String managersContent = mdsRestClient.getAllManagers();
            if (!StringUtils.hasText(managersContent)) {
                LOGGER.error("Failed to get Managers.");
                return;
            }
            List<Manager> managers = objectMapper.readValue(managersContent, new TypeReference<List<Manager>>() {});
            if (!managers.isEmpty()) {
                Map<String, Manager> managerMap = managers.stream()
                    .filter(m -> taskConfig.getManagerTypes().contains(m.getType().name()))
                    .peek(m -> LOGGER.info("[{}] {}", m.getType(), m.getName()))
                    .collect(Collectors.toMap(Manager::getName, m -> m));
                LOGGER.info("Fetched {} managers {} from Core.", managerMap.size(), taskConfig.getManagerTypes());

                // 1 - Remove managers that no longer exist
                managerNodeCache.keySet().retainAll(managerMap.keySet());

                // 2 - Update manager
                managerMap.forEach((key, value) -> {
                    ManagerNode context = managerNodeCache.get(key);
                    if (context != null) {
                        context.setManager(value);
                    } else {
                        managerNodeCache.put(key, new ManagerNode(value));
                    }
                });

                // 3 - Update devices
                managerNodeCache.values().forEach(context -> {
                    scheduler.submit(() -> syncManagerNode(context));
                });
            }

            LOGGER.info("Finish synchronization of all managers.");
        } catch (Exception e) {
            LOGGER.error("Synchronization task failed", e);
        }
    }

    public void syncManagerNode(ManagerNode context) {
        if (context.getManager().getType() == ManagerType.AMS) {
            amsSyncService.syncDevices(context);
        } else if (context.getManager().getType() == ManagerType.NAV) {
            if (CollectionUtils.isEmpty(taskConfig.getIntentTypes())) {
                context.clearDevices();
                LOGGER.warn(
                    "Skip device synchronization for Manager {}, configuration 'joa.mds.sync-task.intent-types' is empty.",
                    context.getManager());
                return;
            }
            navSyncService.syncDevices(context);
        } else {
            LOGGER.warn("Skip device synchronization for Manager {}", context.getManager());
        }
    }

    @PreDestroy
    public void destroy() {
        scheduler.shutdown();
    }

    public Map<String, ManagerNode> getManagerNodeCache() {
        return managerNodeCache;
    }

    public Optional<Manager> getManager(String managerName) {
        return Optional.ofNullable(managerNodeCache.get(managerName))
            .map(ManagerNode::getManager);
    }

    public Optional<Device> getDevice(String identifier) {
        return managerNodeCache.values().stream()
            .flatMap(n -> n.getDevices().stream())
            .filter(d -> identifier.equals(d.getName()) || identifier.equals(d.getIpAddress()))
            .findFirst();
    }

    public Optional<List<Device>> getDevicesByManager(String managerName) {
        return Optional.ofNullable(managerNodeCache.get(managerName))
            .map(ManagerNode::getDevices);
    }

    public List<Manager> getManagers() {
        return managerNodeCache.values().stream()
            .map(ManagerNode::getManager)
            .collect(Collectors.toList());
    }
}


