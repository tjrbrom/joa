package com.x.hhqs.joa.tools.nacos;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.x.hhqs.joa.tools.nacos.rest.NacosConfig;
import com.x.hhqs.joa.tools.nacos.rest.NacosRestClient;

public class NacosTool {
    private static final Logger LOGGER = LoggerFactory.getLogger(NacosTool.class);

    private static final String TAG = "tag";

    private Map<String, String> envMap;
    private NacosRestClient client;

    public NacosTool(NacosRestClient restClient) {
        if (this.envMap == null) {
            this.envMap = System.getenv();
        }
        this.client = restClient;
    }

    public boolean checkNacosHealthUtilTimeout(long timeoutInSeconds, long retryIntervalSeconds) {
        boolean isHealthy = false;
        Instant startTime = Instant.now();
        do {
            try {
                isHealthy = client.checkServerHealth();
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }

            if (!isHealthy) {
                Duration elapsedTime = Duration.between(startTime, Instant.now());
                if (elapsedTime.getSeconds() >= timeoutInSeconds) {
                    LOGGER.warn("Timeout reached. Exiting wait for Nacos server to be connectable.");
                    break;
                }

                try {
                    Thread.sleep(retryIntervalSeconds * 1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            }

        } while (!isHealthy);

        return isHealthy;
    }

    public List<InitConfig> loadInitConfigs(Path configPath) throws Exception {
        InitConfigsLoader configLoader = new InitConfigsLoader();
        configLoader.setValueMap(getEnvMap());

        List<InitConfig> result = new ArrayList<>();
        if (Files.isDirectory(configPath)) {
            List<Path> jsons = Files.list(configPath).filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json"))
                    .collect(Collectors.toList());

            for (Path json : jsons) {
                result.addAll(configLoader.loadNacosConfigs(json));
            }
        } else {
            result.addAll(configLoader.loadNacosConfigs(configPath));
        }

        return result;
    }

    public void updateNacos(List<InitConfig> initConfigs) throws Exception {
        Map<String, List<NacosConfig>> cacheMap = new HashMap<>();

        for (InitConfig init : initConfigs) {
            LOGGER.info("Configuration: {}", init);
            String namespaceId = init.getNamespaceId();
            List<NacosConfig> remoteConfigs = cacheMap.get(namespaceId);
            if (remoteConfigs == null) {
                remoteConfigs = client.getConfigsInNamespace(namespaceId);
                cacheMap.put(namespaceId, remoteConfigs);
            }

            Optional<NacosConfig> optional = remoteConfigs.stream()
                    .filter(config -> config.getDataId().equals(init.getDataId())
                            && config.getGroup().equals(init.getGroup()))
                    .findFirst();

            if (optional.isEmpty()) {
                LOGGER.info("{} not found in Nacos, creating it.", init.getDataId());
                client.updateOrCreateConfig(toNacosConfig(init));
                LOGGER.info("Create success.");
                continue;
            }

            NacosConfig remoteCfg = optional.get();
            Map<String, String> remoteData = client.getConfigData(init.getDataId(), init.getGroup(), namespaceId);

            if (ifNeedUpdate(init.getTag(), remoteData.get(TAG))) {
                LOGGER.info("{} in Nacos need update, updating it.", init.getDataId());
                String newContent = NacosUtils.mapToContent(mergeData(init, remoteData));
                remoteCfg.setContent(newContent);
                client.updateOrCreateConfig(remoteCfg);
                LOGGER.info("Update success.");
            } else {
                LOGGER.info("{} in Nacos does not need updating.", init.getDataId());
            }
        }
    }

    private NacosConfig toNacosConfig(InitConfig init) {
        NacosConfig config = new NacosConfig();
        config.setDataId(init.getDataId());
        config.setGroup(init.getGroup());
        config.setNamespaceId(init.getNamespaceId());
        config.setType(init.getType());

        Map<String, String> map = new LinkedHashMap<>();
        map.putAll(init.getData());
        map.put(TAG, init.getTag());
        config.setContent(NacosUtils.mapToContent(map));

        return config;
    }

    boolean ifNeedUpdate(String initTag, String remoteTag) {
        if (remoteTag == null) {
            return true;
        }

        try {
            Float.valueOf(remoteTag);
        } catch (Exception e) {
            LOGGER.warn("Ignore invalid Nacos {} value: {}", TAG, remoteTag);
            return true;
        }

        return Float.valueOf(initTag) > Float.valueOf(remoteTag);
    }

    Map<String, String> mergeData(InitConfig init, Map<String, String> remoteData) {
        Map<String, String> map = new LinkedHashMap<>();
        map.putAll(remoteData);
        map.remove(TAG);

        init.getData().forEach(map::putIfAbsent);
        map.put(TAG, init.getTag());
        return map;
    }

    public Map<String, String> getEnvMap() {
        return envMap;
    }

    public void setEnvMap(Map<String, String> envMap) {
        this.envMap = envMap;
    }
}
