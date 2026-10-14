package com.x.hhqs.joa.common.rt1fwk.pojo;

import java.util.HashMap;
import java.util.Map;

public class OssCmdMeta {

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
     * source: intent <ont:ont-type>
     */
    private String serviceType;

    /**
     * olt ip address
     * customized intent attributes <dmf:ip-address>
     */
    private String oltIP;
    /**
     * olt name
     * customized intent attributes <dmf:ne-name>
     */
    private String oltName;

    /**
     * GET ponType FROM intent <xpon-type>mpm-gpon-xgs</xpon-type>
     * in this case, it should be 'mpm-gpon-xgs'
     */
    private String ponType;

    /**
     * range: 1-16
     */
    private String slotNo;
    /**
     * range: 1-16
     */
    private String ponNo;

    /**
     * not set value in joa-rt1common as low efficiency
     */
    private String onuNo;

    /**
     * customer field
     * GET onuName FROM intent  <ont-name>
     */
    private String onuName;
    /**
     * customer field
     * GET onuIP FROM intent <ont-ip>
     */
    private String onuIP;


    /**
     * ONU Authentication
     * from expected* attributes in intent
     */
    private String onuSlid;
    private String onuSerNum;
    private String onuLoid;

    /**
     * nbiMetadata
     * the map contains two aspects.
     *
     * 1/ key from AID: all inputted rt1 aid should be divided as key-value and push into map.
     * 2/ key from param block: all inputted rt1 param should be divided as key-value and push into map.
     */
    private Map<String, String> nbiMetadata = new HashMap();

    /**
     * for LightSpan
     * device name (intent)
     * fiber device name (intent)
     * ont device name (intent)
     *
     * for ISAM
     * agent elementNr (Mobject)
     * slotid, ponid, ontno (Mobject)
     * NE name (Full friendlyname, AMS Restful),  (Restful API of AMS webfwk is preferred for JBBRI TOPA to communicate with AMS )
     * (ONU port) ont uni id
     */
    private Map<String, String> sbiMetadata = new HashMap<>();

    /**
     * Partition Access Profile for the device
     * APAPPS-4929 US3 - deal with RT1 command under control with PAP
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

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
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

    public String getSlotNo() {
        return slotNo;
    }

    public void setSlotNo(String slotNo) {
        this.slotNo = slotNo;
    }

    public String getPonNo() {
        return ponNo;
    }

    public void setPonNo(String ponNo) {
        this.ponNo = ponNo;
    }

    public String getOnuNo() {
        return onuNo;
    }

    public void setOnuNo(String onuNo) {
        this.onuNo = onuNo;
    }

    public String getOnuName() {
        return onuName;
    }

    public void setOnuName(String onuName) {
        this.onuName = onuName;
    }

    public String getOnuIP() {
        return onuIP;
    }

    public void setOnuIP(String onuIP) {
        this.onuIP = onuIP;
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

    public Map<String, String> getNbiMetadata() {
        return nbiMetadata;
    }

    public void setNbiMetadata(Map<String, String> nbiMetadata) {
        this.nbiMetadata = nbiMetadata;
    }

    public Map<String, String> getSbiMetadata() {
        return sbiMetadata;
    }

    public void setSbiMetadata(Map<String, String> sbiMetadata) {
        this.sbiMetadata = sbiMetadata;
    }

    @Override
    public String toString() {
        return "OssCmdMeta{" +
                "oltType='" + oltType + '\'' +
                ", oltVersion='" + oltVersion + '\'' +
                ", serviceType='" + serviceType + '\'' +
                ", oltIP='" + oltIP + '\'' +
                ", oltName='" + oltName + '\'' +
                ", ponType='" + ponType + '\'' +
                ", slotPort='" + slotNo + '\'' +
                ", ponPort='" + ponNo + '\'' +
                ", onuNo='" + onuNo + '\'' +
                ", onuName='" + onuName + '\'' +
                ", onuIP='" + onuIP + '\'' +
                ", onuSlid='" + onuSlid + '\'' +
                ", onuSerNum='" + onuSerNum + '\'' +
                ", onuLoid='" + onuLoid + '\'' +
                ", pap='" + pap + '\'' +
                ", nbiMetadata=" + nbiMetadata +
                ", sbiMetadata=" + sbiMetadata +
                '}';
    }
}
