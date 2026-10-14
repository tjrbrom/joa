package com.x.hhqs.joa.rmuid.model;

public class HamObject {

    private String originHamId;

    private String hamId;

    private String resourceType;

    private String deviceIp;

    private Integer onuId;

    public HamObject(String hamId, String resourceType) {
        this.originHamId = hamId;
        this.hamId = hamId;
        this.resourceType = resourceType;
    }

    public String getHamId() {
        return hamId;
    }

    public void setHamId(String hamId) {
        this.hamId = hamId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getDeviceIp() {
        return deviceIp;
    }

    public void setDeviceIp(String deviceIp) {
        this.deviceIp = deviceIp;
    }

    public Integer getOnuId() {
        return onuId;
    }

    public void setOnuId(Integer onuId) {
        this.onuId = onuId;
    }

    public String getOriginHamId() {
        return originHamId;
    }
}
