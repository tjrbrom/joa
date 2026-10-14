package com.x.hhqs.joa.mds.controller;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.x.hhqs.joa.mds.mapper.ManagerMapper;
import com.x.hhqs.joa.mds.sync.MdsContextService;
import com.x.hhqs.joa.mds.sync.SyncTaskConfig;

@RestController
@RequestMapping("/api/v1/app")
public class AppInfoController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppInfoController.class);

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired(required = false)
    private BuildProperties buildProperties;

    @Autowired(required = false)
    private SyncTaskConfig taskConfig;

    @Autowired
    private ConfigurableEnvironment env;

    @Autowired(required = false)
    private MdsContextService mdsContextService;

    @PostConstruct
    public void init() {
        if (buildProperties != null) {
            LOGGER.info("Version: {}, Build Time: {}", buildProperties.getVersion(), buildProperties.getTime());
        }
    }

    @GetMapping("/info")
    public ResponseEntity<?> getAppInfo() {
        Map<String, Map<String, Object>> map = new LinkedHashMap<>();
        if (buildProperties != null) {
            map.put("build", buildInfo(buildProperties));
        }

        if (taskConfig != null) {
            map.put("task", taskInfo(taskConfig));
        }

        if (mdsContextService != null) {
            map.put("managers", managerInfo());
        }

        return ResponseEntity.ok(map);
    }

    private Map<String, Object> managerInfo() {
        Map<String, Object> map = new LinkedHashMap<>();
        mdsContextService.getManagers().forEach(manager -> {
            map.put(manager.getName(), ManagerMapper.toDto(manager));
        });

        return map;
    }

    private Map<String, Object> taskInfo(SyncTaskConfig config) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("enabled", config.isEnabled());
        map.put("intervalMinutes", config.getIntervalMinutes());
        map.put("managerTypes", config.getManagerTypes());
        map.put("intentTypes", config.getIntentTypes());
        if (env.getProperty("ams.useConfiguredCredentials", Boolean.class, false)) {
            map.put("ams.useConfiguredCredentials", true);
            map.put("ams.username", env.getProperty("ams.username"));
        }

        return map;
    }

    private Map<String, Object> buildInfo(BuildProperties properties) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", properties.getArtifact());
        map.put("version", properties.getVersion());
        map.put("build-time", DATE_TIME_FORMATTER.format(properties.getTime().atZone(ZoneId.systemDefault())));

        return map;
    }

}
