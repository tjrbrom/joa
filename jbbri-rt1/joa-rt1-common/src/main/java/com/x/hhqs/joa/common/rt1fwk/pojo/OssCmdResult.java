package com.x.hhqs.joa.common.rt1fwk.pojo;

import com.x.hhqs.joa.common.util.CommonConstants;

/**
 * the result retuned by command handler
 * tle,sum of results, need object
 */
public class OssCmdResult {


    private boolean success = true;

    private String errorCode = "";

    /**
     * errorDesc need mapped from properties
     */
    private String errorDesc = "";

    private String errorDetail = "";

    //for query commands
    private String rtrvTitle; //mandatory for succeed LST commands;
    private String[][] rtrvArray;

    public OssCmdResult(boolean success) {
        this.success = success;
    }

    public OssCmdResult(boolean success, String errorCode, String errorDetail) {
        this.success = success;
        this.errorCode = errorCode;
        this.errorDesc = CommonConstants.ENDESC_PREFIX + errorCode;
        this.errorDetail = errorDetail;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorDesc() {
        return errorDesc;
    }

    public void setErrorDesc(String errorDesc) {
        this.errorDesc = errorDesc;
    }

    public String getErrorDetail() {
        return errorDetail;
    }

    public void setErrorDetail(String errorDetail) {
        this.errorDetail = errorDetail;
    }

    public String[][] getRtrvArray() {
        return rtrvArray;
    }

    public void setRtrvArray(String[][] rtrvArray) {
        this.rtrvArray = rtrvArray;
    }

    public String getRtrvTitle() {
        return rtrvTitle;
    }

    public void setRtrvTitle(String rtrvTitle) {
        this.rtrvTitle = rtrvTitle;
    }

    @Override
    public String toString() {
        return "OssCmdResult{" +
                "ifSuccess=" + success +
                ", errorCode='" + errorCode + '\'' +
                ", errorDesc='" + errorDesc + '\'' +
                ", errorDetail='" + errorDetail + '\'' +
                '}';
    }


}
