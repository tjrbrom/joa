package com.x.hhqs.joa.common.rt1fwk.pojo;

public class DeviceInfo {

    //olt info
    /**
     * get oltType from <ibn:intent-type>device-mf</ibn:intent-type>
     * in this case, it is device-mf
     */
    private String oltType;
    /**
     * get oltVersion from <dfx:ihub-version>23.3</dfx:ihub-version>
     * in this case, it is 23.3
     */
    private String oltVersion;

    /**
     * olt ip address
     */
    private String oltIP;
    /**
     * olt name
     */
    private String oltName;

    /**
     * intent name in device-fx
     */
    private String oltTargetName;

    //pon info
    /**
     * GET ponType FROM <xpon-type>mpm-gpon-xgs</xpon-type>, in this case, it should be 'mpm-gpon-xgs'
     */
    private String ponType;
    /**
     * intent name in fiber
     */
    private String ponTargetName;
    /**
     * ponid,like LT1.PON4
     * just with value when nbi command have ONUIP=XXXX format
     */
    private String ponId;
    //onu info
    /**
     * <ont:ont-type>G-010G-A</ont:ont-type>
     */
    private String onuType;
    /**
     * onu id, scope is 0-127 for LS
     */
    private String onuAid;
    //for Light span
    /**
     * intent name for onu
     */
    private String onuTargetName;
    /**
     * customized intent attribute
     */
    private String onuIP;
    /**
     * customized intent attribute
     */
    private String onuName;

    /**
     * ONU Authentication
     * from expected* attributes in intent
     */
    private String onuSlid;
    private String onuSerNum;
    private String onuLoid;

    /**
     * Partition Access Profile
     */
    private String pap;

    public String getPap() {
        return pap;
    }

    public void setPap(String pap) {
        this.pap = pap;
    }

    public String getOltType() {
        return oltType;
    }

    public void setOltType(String oltType) {
        this.oltType = oltType;
    }

    public String getOltVersion() {
        return oltVersion;
    }

    public void setOltVersion(String oltVersion) {
        this.oltVersion = oltVersion;
    }

    public String getOltTargetName() {
        return oltTargetName;
    }

    public void setOltTargetName(String oltTargetName) {
        this.oltTargetName = oltTargetName;
    }


    public String getOltIP() {
        return oltIP;
    }

    public void setOltIP(String oltIP) {
        this.oltIP = oltIP;
    }

    public String getOltName() {
        return oltName;
    }

    public void setOltName(String oltName) {
        this.oltName = oltName;
    }

    public String getPonType() {
        return ponType;
    }

    public void setPonType(String ponType) {
        this.ponType = ponType;
    }

    public String getPonTargetName() {
        return ponTargetName;
    }

    public void setPonTargetName(String ponTargetName) {
        this.ponTargetName = ponTargetName;
    }

    public String getOnuType() {
        return onuType;
    }

    public void setOnuType(String onuType) {
        this.onuType = onuType;
    }

    public String getOnuAid() {
        return onuAid;
    }

    public void setOnuAid(String onuAid) {
        this.onuAid = onuAid;
    }

    public String getOnuTargetName() {
        return onuTargetName;
    }

    public void setOnuTargetName(String onuTargetName) {
        this.onuTargetName = onuTargetName;
    }


    public String getPonId() {
        return ponId;
    }

    public void setPonId(String ponId) {
        this.ponId = ponId;
    }

    public String getOnuIP() {
        return onuIP;
    }

    public void setOnuIP(String onuIP) {
        this.onuIP = onuIP;
    }

    public String getOnuName() {
        return onuName;
    }

    public void setOnuName(String onuName) {
        this.onuName = onuName;
    }

    public String getOnuSlid() {
        return onuSlid;
    }

    public void setOnuSlid(String onuSlid) {
        this.onuSlid = onuSlid;
    }

    public String getOnuSerNum() {
        return onuSerNum;
    }

    public void setOnuSerNum(String onuSerNum) {
        this.onuSerNum = onuSerNum;
    }

    public String getOnuLoid() {
        return onuLoid;
    }

    public void setOnuLoid(String onuLoid) {
        this.onuLoid = onuLoid;
    }
}
