package com.x.hhqs.joa.mds.dto;

public class DeviceDto {

    private String name;

    private String managerName;

    private String managerType;

    private String familyTypeRelease;

    private String ipAddress;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public String getFamilyTypeRelease() {
        return familyTypeRelease;
    }

    public void setFamilyTypeRelease(String familyTypeRelease) {
        this.familyTypeRelease = familyTypeRelease;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getManagerType() {
        return managerType;
    }

    public void setManagerType(String managerType) {
        this.managerType = managerType;
    }

}
