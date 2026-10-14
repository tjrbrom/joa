package com.x.hhqs.joa.mds.dto;

public class NavDeviceDto extends DeviceDto {

    private String deviceVersion;

    private String hardwareType;

    public String getDeviceVersion() {
        return deviceVersion;
    }

    public void setDeviceVersion(String deviceVersion) {
        this.deviceVersion = deviceVersion;
    }

    public String getHardwareType() {
        return hardwareType;
    }

    public void setHardwareType(String hardwareType) {
        this.hardwareType = hardwareType;
    }

}
