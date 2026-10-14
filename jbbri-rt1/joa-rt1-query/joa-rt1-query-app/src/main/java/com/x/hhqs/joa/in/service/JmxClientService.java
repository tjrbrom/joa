package com.x.hhqs.joa.in.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ams jmx client
 */
public class JmxClientService {

    private static final Logger LOG = LoggerFactory.getLogger(JmxClientService.class);

    private String host;

   public JmxClientService(String host) {
        this.host = host;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Map<String, String>> getAllDevice(Map<String, String> topoMap) {
        return withJmxConnection(jmxConnection -> {
            ObjectName mbeanName = new ObjectName("application.jbbri.ctin:service=neCTINService");
            String[] moAttributeArr = new String[]{"name", "ipAddress", "productAndRelease",
                    "activeSoftwareVersion", "nodeModel", "parent_id", "connectivityState", "aliasName"};
            List<String> neMoIdList = ((List<Long>) jmxConnection.invoke(mbeanName, "getNeIdList", null, null))
                    .stream().map(neId -> "/neNr=" + neId).collect(Collectors.toList());
            Map<String, Map<String, String>> allDeviceMap = (Map<String, Map<String, String>>) jmxConnection.invoke(
                    mbeanName,
                    "getAppMoAttValByMObjectIdList",
                    new Object[]{"container-ne", neMoIdList, "nelist", moAttributeArr},
                    new String[]{"java.lang.String", "java.util.List", "java.lang.String", "[Ljava.lang.String;"}
            );

            // add group info to topoMap
            List<String> parentIdList = allDeviceMap.values().stream()
                    .map(deviceInfo -> deviceInfo.get("parent_id"))
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            for (Iterator<String> iterator = parentIdList.iterator(); iterator.hasNext(); ) {
                String parentId =  iterator.next();
                String groupName = (String) jmxConnection.invoke(
                        mbeanName,
                        "getAppMoAttValByMObjectId",
                        new Object[]{"group", parentId, "nelist", "fullFriendlyName"},
                        new String[]{"java.lang.String", "java.lang.String", "java.lang.String", "java.lang.String"}
                );
                topoMap.put(parentId, "/"+groupName);
            }

            return allDeviceMap;
        });
    }

    private <T> T withJmxConnection(JmxCallback<T> callback) {
        JMXConnector jmxConnector = null;
        try {
            // 10.99.88.183
            JMXServiceURL serviceURL = new JMXServiceURL("service:jmx:remote://" + host + ":9999");
            Map<String, Object> env = new HashMap<>();
            env.put(JMXConnector.CREDENTIALS, new String[]{"system", "4x5@system"});

            jmxConnector = JMXConnectorFactory.connect(serviceURL, env);
            MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();
            return callback.doWithConnection(connection);
        } catch (Exception e) {
            throw new RuntimeException("JMX operation failed", e);
        } finally {
            if (jmxConnector != null) {
                try {
                    jmxConnector.close();
                } catch (Exception e) {
                    LOG.error("Failed to close JMX connection: " + e.getMessage());
                }
            }
        }
    }

    @FunctionalInterface
    public interface JmxCallback<T> {
        T doWithConnection(MBeanServerConnection connection) throws Exception;
    }
}
