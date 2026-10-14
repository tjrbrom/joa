package com.x.hhqs.joa.alarm.dto;

import com.x.hhqs.joa.alarm.model.ams.AckSyncAlarmFileResult;

import java.util.List;

public class OASAlarmMessageInfo {
    public String msgType;
    public String from;
    public String reqId;
    public boolean result;
    public String msg;
    public AckSyncAlarmFileResult ackSyncAlarmFileResult;
    public List<OasAlarmInfo> oasAlarmInfos;

    public OASAlarmMessageInfo() {
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getReqId() {
        return reqId;
    }

    public void setReqId(String reqId) {
        this.reqId = reqId;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<OasAlarmInfo> getOmcAlarmVOs() {
        return oasAlarmInfos;
    }

    public void setOmcAlarmVOs(List<OasAlarmInfo> oasAlarmInfos) {
        this.oasAlarmInfos = oasAlarmInfos;
    }

    public AckSyncAlarmFileResult getAckSyncAlarmFileResult() {
        return ackSyncAlarmFileResult;
    }

    public void setAckSyncAlarmFileResult(AckSyncAlarmFileResult ackSyncAlarmFileResult) {
        this.ackSyncAlarmFileResult = ackSyncAlarmFileResult;
    }
}
