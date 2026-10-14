package com.x.hhqs.joa.common.sharedfileservice.config;

import com.x.hhqs.joa.common.sharedfileservice.utils.FileServerInfo;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "core.webdav")
public class WebdavFileServiceProperties {
    public static final String SLASH = "/";
    public static final String SHAREDFS = "sharedfs";

    public static final String DEFAULT_USERNAME = "admin";
    public static final String DEFAULT_PASSWORD = "";
    public static final String DEFAULT_ROOT_DIR = "/webdav";
    public static final String DASH = "-";
    public static final String DEFAULT_URL = "https://localhost:5443";

    private String username;
    private String password;
    private String url;
    private String instancePrefix;
    private String instanceName;
    private String sharedFileServerExternalUrl;
    private String k8sPublicIp;
    private String stagingArea;
    private List<SystemProperty> systemProperties = new ArrayList<>();

    public static class SystemProperty {
        private String name;
        private String value;

        public SystemProperty() {
        }

        public SystemProperty(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    @PostConstruct
    public void init() {
        for (SystemProperty systemProperty : systemProperties) {
            if (systemProperty != null && systemProperty.getName() != null && systemProperty.getValue() != null) {
                System.setProperty(systemProperty.getName(), String.valueOf(systemProperty.getValue()));
            }
        }
    }

    public String getFullInstanceName() {
        if (instancePrefix != null && !instancePrefix.isEmpty()) {
            return instancePrefix + "-" + instanceName;
        } else {
            return instanceName;
        }
    }

    public String buildDefaultDirectory() {
        StringBuilder defaultDirectory = new StringBuilder();
        if (instancePrefix != null && !instancePrefix.isEmpty()) {
            defaultDirectory.append(instancePrefix).append(DASH);
            defaultDirectory.append(instanceName).append(SLASH);
            defaultDirectory.append(SHAREDFS);
        } else {
            defaultDirectory.append(instanceName).append(SLASH);
            defaultDirectory.append(SHAREDFS);
        }
        return defaultDirectory.toString();
    }

    public FileServerInfo getFileServerInfo() {
        return new FileServerInfo(username, password, url, buildDefaultDirectory(), getFullInstanceName());
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getInstancePrefix() {
        return instancePrefix;
    }

    public void setInstancePrefix(String instancePrefix) {
        this.instancePrefix = instancePrefix;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getSharedFileServerExternalUrl() {
        return sharedFileServerExternalUrl;
    }

    public void setSharedFileServerExternalUrl(String sharedFileServerExternalUrl) {
        this.sharedFileServerExternalUrl = sharedFileServerExternalUrl;
    }

    public String getK8sPublicIp() {
        return k8sPublicIp;
    }

    public void setK8sPublicIp(String k8sPublicIp) {
        this.k8sPublicIp = k8sPublicIp;
    }

    public String getStagingArea() {
        return stagingArea;
    }

    public void setStagingArea(String stagingArea) {
        this.stagingArea = stagingArea;
    }

    public List<SystemProperty> getSystemProperties() {
        return systemProperties;
    }

    public void setSystemProperties(List<SystemProperty> systemProperties) {
        this.systemProperties = systemProperties;
    }
}
