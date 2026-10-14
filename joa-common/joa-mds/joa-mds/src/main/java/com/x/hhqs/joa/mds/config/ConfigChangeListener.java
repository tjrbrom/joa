package com.x.hhqs.joa.mds.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;
import com.x.hhqs.joa.mds.sync.MdsContextService;

@Component
public class ConfigChangeListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigChangeListener.class);

    @Autowired
    private MdsContextService mdsContextService;

    @Autowired
    private ConfigurableEnvironment environment;

    private final Map<String, String> cachedProperties = new HashMap<>();

    public Map<String, String> getCachedProperties() {
        return cachedProperties;
    }

    private final List<String> keysToWatch = List.of(
        "joa.mds.sync-task.interval-minutes");

    @PostConstruct
    public void initCache() {
        for (String key : keysToWatch) {
            String value = environment.getProperty(key);
            cachedProperties.put(key, value);
        }
    }

    @EventListener(EnvironmentChangeEvent.class)
    public void onConfigChange(EnvironmentChangeEvent event) {
        for (String key : event.getKeys()) {
            LOGGER.info("EvnChange key:{} value:{}", key, environment.getProperty(key));

            if (!keysToWatch.contains(key)) {
                continue;
            }

            String newValue = environment.getProperty(key);
            String oldValue = cachedProperties.get(key);
            if (Objects.equals(newValue, oldValue)) {
                continue;
            }
            cachedProperties.put(key, newValue);

            if ("joa.mds.sync-task.interval-minutes".equalsIgnoreCase(key)) {
                mdsContextService.scheduleTask(Long.valueOf(newValue));
            }
        }
    }
}
