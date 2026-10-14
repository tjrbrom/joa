package com.x.hhqs.joa.in.vo;

import com.x.hhqs.joa.in.consts.NbiConsts;
import com.x.hhqs.joa.in.vo.customField.CustomField;

@CustomField(ossName = "LST-DEVICE")
public class Device implements Comparable<Device> {

    @CustomField(fieldName = "deviceTarget")
    private String target;

    @NbiResponseDefinition(fieldName = "DEVNAME")
    private String devName;

    @NbiResponseDefinition(fieldName = "DEVIP")
    private String devIp;

    @NbiResponseDefinition(fieldName = "DT")
    private String dt;

    @NbiResponseDefinition(fieldName = "DEVER")
    private String deVer;

    @NbiResponseDefinition(fieldName = "", ignored = true)
    private String pap;

    @NbiResponseDefinition(fieldName = "TOPOLOC", nbiSpec = NbiConsts.NBI_SPEC_CM)
    private String topoLoc;

    @NbiResponseDefinition(fieldName = "DSTAT", nbiSpec = NbiConsts.NBI_SPEC_CM)
    private String devStat;

    @NbiResponseDefinition(fieldName = "ALIAS", nbiSpec = NbiConsts.NBI_SPEC_CM)
    private String alias;

    public Device() {
        super();
    }

    public Device(String devName, String devIp, String dt, String deVer, String topoLoc, String devStat, String alias) {
        this.devName = devName;
        this.devIp = devIp;
        this.dt = dt;
        this.deVer = deVer;
        this.topoLoc = topoLoc;
        this.devStat = devStat;
        this.alias = alias;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getDevName() {
        return devName;
    }

    public void setDevName(String devName) {
        this.devName = devName;
    }

    public String getDevIp() {
        return devIp;
    }

    public void setDevIp(String devIp) {
        this.devIp = devIp;
    }

    public String getDt() {
        return dt;
    }

    public void setDt(String dt) {
        this.dt = dt;
    }

    public String getDeVer() {
        return deVer;
    }

    public void setDeVer(String deVer) {
        this.deVer = deVer;
    }

    public String getPap() {
        return pap;
    }

    public void setPap(String pap) {
        this.pap = pap;
    }

    public String getTopoLoc() {
        return topoLoc;
    }

    public void setTopoLoc(String topoLoc) {
        this.topoLoc = topoLoc;
    }

    public String getDevStat() {
        return devStat;
    }

    public void setDevStat(String devStat) {
        this.devStat = devStat;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    @Override
    public int compareTo(Device o) {
        return this.getDevIp().hashCode() - o.getDevIp().hashCode();
    }

    @Override
    public String toString() {
        return "Device{" +
                "target='" + target + '\'' +
                ", devName='" + devName + '\'' +
                ", devIp='" + devIp + '\'' +
                ", dt='" + dt + '\'' +
                ", deVer='" + deVer + '\'' +
                ", pap='" + pap + '\'' +
                ", topoLoc='" + topoLoc + '\'' +
                ", devStat='" + devStat + '\'' +
                ", alias='" + alias + '\'' +
                '}';
    }

}
