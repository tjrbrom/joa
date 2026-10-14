package com.x.hhqs.joa.mds.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Device {

    private String name;

    private String managerName;

    private ManagerType managerType;

    private String familyTypeRelease;

    private Long id;

    private String ipAddress;

    private String subrackModel;

    private String subType;

    private String productAndRelease;
    
    private String deviceVersion;
    
    private String hardwareType;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubrackModel() {
        return subrackModel;
    }

    public void setSubrackModel(String subrackModel) {
        this.subrackModel = subrackModel;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public String getProductAndRelease() {
        return productAndRelease;
    }

    public void setProductAndRelease(String productAndRelease) {
        this.productAndRelease = productAndRelease;
    }

    public ManagerType getManagerType() {
        return managerType;
    }

    public void setManagerType(ManagerType managerType) {
        this.managerType = managerType;
    }

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
