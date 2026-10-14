package com.x.hhqs.joa.mds.sync;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.http.conn.util.InetAddressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.x.hhqs.joa.mds.emsclient.ac.MdsRestClient;
import com.x.hhqs.joa.mds.emsclient.ams.AmsRestClient;
import com.x.hhqs.joa.mds.emsclient.ams.AmsRestClientConfig;
import com.x.hhqs.joa.mds.model.Device;
import com.x.hhqs.joa.mds.model.Manager;
import com.x.hhqs.joa.mds.model.ManagerNode;
import com.x.hhqs.joa.mds.model.ManagerType;

@Service
public class AmsSyncService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AmsSyncService.class);

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Set<String> attributeNames = Set.of(
        "name",
        "ipAddress",
        "id",
        "subrackModel",
        "subType",
        "productAndRelease");

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MdsRestClient mdsRestClient;

    @Autowired
    private Function<AmsRestClientConfig, AmsRestClient> amsRestClientFactory;

    @Autowired
    private CredentialService credentialService;

    public void syncDevices(ManagerNode context) {
        LOGGER.info("Starting device synchronization for Manager {}", context.getManager());

        AmsRestClient amsClient = null;
        try {
            Manager manager = context.getManager();
            manager.setLastSyncTime(DATE_TIME_FORMATTER.format(LocalDateTime.now()));

            String devicesContent = mdsRestClient.getDevicesWithManagerName(manager.getName());
            if (!StringUtils.hasText(devicesContent)) {
                LOGGER.error("Failed to get devices from Core, manager:" + manager.getName());
                return;
            }

            List<Device> devices = objectMapper.readValue(devicesContent, new TypeReference<List<Device>>() {});
            LOGGER.info("Fetched {} devices from Core.", devices.size());

            amsClient = createAmsRestClient(context.getManager());
            amsClient.init();

            Map<String, Device> deviceMap = devices.stream()
                .collect(Collectors.toMap(d -> "NE:" + d.getName(), d -> d));

            Map<String, Map<String, Object>> deviceDetailsMap =
                amsClient.getAttributesByFriendlyNames(deviceMap.keySet(), attributeNames);

            deviceMap.forEach((name, device) -> {
                Map<String, Object> details = deviceDetailsMap.get(name);
                device.setManagerType(ManagerType.AMS);
                device.setId(Long.valueOf(details.get("id").toString()));
                device.setIpAddress((String) details.get("ipAddress"));
                device.setSubrackModel((String) details.get("subrackModel"));
                device.setSubType((String) details.get("subType"));
                device.setProductAndRelease((String) details.get("productAndRelease"));
            });
            context.setDevices(deviceMap.values().stream().collect(Collectors.toList()));

            manager.setLastSuccessSyncTime(DATE_TIME_FORMATTER.format(LocalDateTime.now()));
            LOGGER.info("Device synchronization for {} completed.", context.getManager());
        } catch (Exception e) {
            LOGGER.error("Device synchronization failed for Manager:" + context.getManager().getName(), e);
        } finally {
            if (amsClient != null) {
                amsClient.close();
            }
        }
    }

    static String getBaseUrl(Manager manager) {
        return UriComponentsBuilder
            .fromUriString("{protocol}://{activeIp}:{port}")
            .buildAndExpand(manager.getProtocol(), urlFormatForIpv6Address(manager.getActiveIp()), manager.getPort())
            .toUriString();
    }

    static String urlFormatForIpv6Address(String ipAdress) {
        if (ipAdress.charAt(0) == '[' && ipAdress.length() > 2 && ipAdress.charAt(ipAdress.length() - 1) == ']') {
            // already enclosed inside [ ]
            return ipAdress;
        } else if (InetAddressUtils.isIPv6Address(ipAdress)) {
            return "[" + ipAdress + "]";
        }
        return ipAdress;
    }

    AmsRestClient createAmsRestClient(Manager manager) {
        AmsRestClientConfig clientConfig = buildAmsRestClientConfig(manager);
        AmsRestClient amsRestClient = amsRestClientFactory.apply(clientConfig);
        return amsRestClient;
    }

    AmsRestClientConfig buildAmsRestClientConfig(Manager manager) {
        AmsRestClientConfig.Builder builder = new AmsRestClientConfig.Builder();
        builder.baseUrl(getBaseUrl(manager))
            .credential(credentialService.getUsername(manager), credentialService.getPassword(manager))
            .serverName(manager.getName());

        AmsRestClientConfig clientConfig = builder.build();
        return clientConfig;
    }

}
