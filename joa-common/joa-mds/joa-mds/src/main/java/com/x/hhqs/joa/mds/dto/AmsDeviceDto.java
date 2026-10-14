package com.x.hhqs.joa.mds.dto;

public class AmsDeviceDto extends DeviceDto {

    private Long id;

    private String subrackModel;

    private String subType;

    private String productAndRelease;

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
}
