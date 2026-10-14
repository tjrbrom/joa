package com.x.hhqs.joa.common.rt1fwk.exception;

public class OssCommandException extends Exception {

    private String errCode;
    private String errDetail;

    public OssCommandException(String errCode, String errDetail) {
        super(errDetail);
        this.errCode = errCode;
        this.errDetail = errDetail;
    }

    public OssCommandException(String errCode, String errDetail, Throwable cause) {
        super(errDetail, cause);
        this.errCode = errCode;
        this.errDetail = errDetail;
    }

    public String getErrCode() {
        return errCode;
    }

    public String getErrDetail() {
        return errDetail;
    }

}
