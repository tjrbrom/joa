package com.x.hhqs.joa.tools.nacos;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.x.hhqs.joa.tools.nacos.rest.NacosRestClient;

public class NacosToolMain {
    private static final Logger LOGGER = LoggerFactory.getLogger(NacosToolMain.class);

    public static final String CONFIG_PATH = "CONFIG_PATH";
    public static final String TIMEOUT_SECONDS = "CHECK_HEALTH_TIMEOUT_SECONDS";
    public static final String RETRY_INTERVAL_SECONDS = "CHECK_HEALTH_RETRY_INTERVAL_SECONDS";
    public static final String NACOS_SERVER_ADDR = "NACOS_SERVER_ADDR";

    private static final String DEFAULT_CONFIG_PATH = "/joa/nacos/config";
    private static final long DEFAULT_RETRY_INTERVAL_SECONDS = 10;
    private static final long DEFAULT_TIMEOUT_SECONDS = 900;

    String nacosUrl = "http://nacos-cs:8848";
    String configPath = DEFAULT_CONFIG_PATH;
    long timeoutInSeconds = DEFAULT_TIMEOUT_SECONDS;
    long retryIntervalSeconds = DEFAULT_RETRY_INTERVAL_SECONDS;

    private Map<String, String> envMap;

    public void run() throws Exception {
        this.envMap = loadEnvMap();
        initVariables(envMap);

        NacosTool nacosTool = createNacosTool();
        nacosTool.setEnvMap(envMap);

        List<InitConfig> initConfigs = nacosTool.loadInitConfigs(Path.of(configPath));
        for (InitConfig config : initConfigs) {
            try {
                Float.parseFloat(config.getTag());
            } catch (Exception e) {
                throw new Exception("Invalid tag value of " + config.getDataId() + ": " + config.getTag());
            }
        }

        boolean isHealthy = nacosTool.checkNacosHealthUtilTimeout(timeoutInSeconds, retryIntervalSeconds);
        if (!isHealthy) {
            throw new Exception("Nacos server is not connectable or healthy.");
        }

        nacosTool.updateNacos(initConfigs);
    }

    private NacosTool createNacosTool() {
        NacosRestClient restClient = createNacosRestClient();
        return new NacosTool(restClient);
    }

    private NacosRestClient createNacosRestClient() {
        return new NacosRestClient(this.nacosUrl);
    }

    void initVariables(Map<String, String> envMap) {
        String timeout = envMap.get(TIMEOUT_SECONDS);
        if (timeout != null) {
            this.timeoutInSeconds = Long.valueOf(timeout);
        }

        String retryInterval = envMap.get(RETRY_INTERVAL_SECONDS);
        if (timeout != null) {
            this.retryIntervalSeconds = Long.valueOf(retryInterval);
        }

        String path = envMap.get(CONFIG_PATH);
        if (path != null) {
            LOGGER.info("CONFIG_PATH: " + path);
            this.configPath = path;
        }

        String url = envMap.get(NACOS_SERVER_ADDR);
        if (url != null) {
            LOGGER.info("NACOS_SERVER_ADDR: " + url);
            this.nacosUrl = NacosUtils.resolveNacosUrl(url);
        }
    }

    Map<String, String> loadEnvMap() {
        Map<String, String> map = new HashMap<>();
        map.putAll(System.getenv());
        return map;
    }

    public static void main(String[] args) {
        try {
            NacosToolMain main = new NacosToolMain();
            main.run();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            System.exit(1);
        }
    }

}
