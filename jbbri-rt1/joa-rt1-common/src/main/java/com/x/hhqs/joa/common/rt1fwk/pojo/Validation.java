package com.x.hhqs.joa.common.rt1fwk.pojo;

import java.util.List;
import java.util.Map;

public class Validation {

    boolean enabled = false;

    ValidAttribute tid;
    List<Map<String, ValidAttribute>> aidGroups;
    ValidAttribute ctag;
    List<Map<String, ValidAttribute>> paramBlocks;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public ValidAttribute getTid() {
        return tid;
    }

    public void setTid(ValidAttribute tid) {
        this.tid = tid;
    }

    public List<Map<String, ValidAttribute>> getAidGroups() {
        return aidGroups;
    }

    public void setAidGroups(List<Map<String, ValidAttribute>> aidGroups) {
        this.aidGroups = aidGroups;
    }

    public ValidAttribute getCtag() {
        return ctag;
    }

    public void setCtag(ValidAttribute ctag) {
        this.ctag = ctag;
    }

    public List<Map<String, ValidAttribute>> getParamBlocks() {
        return paramBlocks;
    }

    public void setParamBlocks(List<Map<String, ValidAttribute>> paramBlocks) {
        this.paramBlocks = paramBlocks;
    }
}
