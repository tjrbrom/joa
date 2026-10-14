package com.x.hhqs.joa.mds.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ManagerDto {
    
    private String name;

    private String type;

    private String ip;

    private String connectivityState;

    private String username;

    private String password;

    private String protocol;

    private int port;

    private String activeIp;

    private String httpUrl;

    private String neGroup;

    private String lastSyncTime;

    private String lastSuccessSyncTime;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getConnectivityState() {
        return connectivityState;
    }

    public void setConnectivityState(String connectivityState) {
        this.connectivityState = connectivityState;
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

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getActiveIp() {
        return activeIp;
    }

    public void setActiveIp(String activeIp) {
        this.activeIp = activeIp;
    }

    public String getHttpUrl() {
        return httpUrl;
    }

    public void setHttpUrl(String httpUrl) {
        this.httpUrl = httpUrl;
    }

    public String getNeGroup() {
        return neGroup;
    }

    public void setNeGroup(String neGroup) {
        this.neGroup = neGroup;
    }

    public String getLastSyncTime() {
        return lastSyncTime;
    }

    public void setLastSyncTime(String lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
    }

    public String getLastSuccessSyncTime() {
        return lastSuccessSyncTime;
    }

    public void setLastSuccessSyncTime(String lastSuccessSyncTime) {
        this.lastSuccessSyncTime = lastSuccessSyncTime;
    }

}
