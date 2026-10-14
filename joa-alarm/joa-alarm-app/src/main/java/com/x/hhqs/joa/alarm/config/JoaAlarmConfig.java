package com.x.hhqs.joa.alarm.config;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "joa-alarm")
public class JoaAlarmConfig {

    private static final long FILE_SIZE_UNIT_MB = 1024 * 1024;

    private static final Logger LOGGER = LoggerFactory.getLogger(JoaAlarmConfig.class);

    private Server server;

    private Integer clientIdleTimeOut;

    private String charset;

    private String role;

    private int maxConnect;

    boolean fileSize100m = false;

    private long maxAlarmFileSize = 100;

    private String version;

    private Long databaseKeepDays = 7L;

    private boolean amsServerIsStart = true;

    private long amsTimeout = 60;

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public Integer getClientIdleTimeOut() {
        return clientIdleTimeOut;
    }

    public void setClientIdleTimeOut(Integer clientIdleTimeOut) {
        this.clientIdleTimeOut = clientIdleTimeOut;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getMaxConnect() {
        return maxConnect;
    }

    public void setMaxConnect(int maxConnect) {
        this.maxConnect = maxConnect;
    }

    public long getMaxAlarmFileSize() {
        return maxAlarmFileSize;
    }

    public void setMaxAlarmFileSize(long maxAlarmFileSize) {
        this.maxAlarmFileSize = maxAlarmFileSize * FILE_SIZE_UNIT_MB;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isFileSize100m() {
        return fileSize100m;
    }

    public void setFileSize100m(boolean fileSize100m) {
        this.fileSize100m = fileSize100m;
    }

    public Long getDatabaseKeepDays() {
        return databaseKeepDays;
    }

    public void setDatabaseKeepDays(Long databaseKeepDays) {
        this.databaseKeepDays = databaseKeepDays;
    }

    public boolean getAmsServerIsStart() {
        return amsServerIsStart;
    }

    public void setAmsServerIsStart(boolean amsServerIsStart) {
        this.amsServerIsStart = amsServerIsStart;
    }

    public long getAmsTimeout() {
        return amsTimeout;
    }

    public void setAmsTimeout(long amsTimeout) {
        this.amsTimeout = amsTimeout;
    }


    @Getter
    @Setter
    public static class Server {

        private Integer port;

        private Integer channelNumber;

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public Integer getChannelNumber() {
            return channelNumber;
        }

        public void setChannelNumber(Integer channelNumber) {
            this.channelNumber = channelNumber;
        }
    }
}
