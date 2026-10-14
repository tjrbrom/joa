package com.x.hhqs.joa.alarm.listener.kafka.processor;


import com.x.hhqs.joa.alarm.dto.OASAlarmMessageInfo;
import com.x.hhqs.joa.alarm.model.ams.SyncAlarmFileRequest;
import com.x.hhqs.joa.alarm.service.manager.SyncAlarmFileChannelManage;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class AsyncActiveAlarmMessageProcessor {

    private final SyncAlarmFileChannelManage syncAlarmFileChannelManage;

    @Autowired
    public AsyncActiveAlarmMessageProcessor(
            SyncAlarmFileChannelManage syncAlarmFileChannelManage
    ) {
        this.syncAlarmFileChannelManage = syncAlarmFileChannelManage;
    }

    public void process(OASAlarmMessageInfo oasAlarmMessageInfo) {
        String reqSyncId = null;
        try {
            if (StringUtils.isEmpty(oasAlarmMessageInfo.getReqId())) return;
            reqSyncId = oasAlarmMessageInfo.getReqId().substring(0, oasAlarmMessageInfo.getReqId().lastIndexOf("_"));
            SyncAlarmFileRequest localStorage = syncAlarmFileChannelManage.getSyncAlarmFileRequest(reqSyncId);
            if (localStorage == null) {
                log.warn("No active request found for reqId: {}", reqSyncId);
                return;
            }
            if (!localStorage.getReqId().equals(oasAlarmMessageInfo.getReqId())) {
                log.warn("not the same client reqId: {}", oasAlarmMessageInfo.getReqId());
                return;
            }
            localStorage.setStatus(oasAlarmMessageInfo.getAckSyncAlarmFileResult().getStatus());
            switch (oasAlarmMessageInfo.getAckSyncAlarmFileResult().getStatus()) {
                case accept:
                    handleAcceptStatus(localStorage, reqSyncId);
                    break;
                case data:
                    handleDataStatus(oasAlarmMessageInfo, localStorage);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            log.error("ams alarm data process error", e);
            if (reqSyncId != null) {
                syncAlarmFileChannelManage.removeRequestFromStorage(reqSyncId);
            }
        }
    }

    private void handleAcceptStatus(SyncAlarmFileRequest localStorage, String reqId) {
        syncAlarmFileChannelManage.saveRequestToStorage(reqId, localStorage);
    }

    private void handleDataStatus(OASAlarmMessageInfo oasAlarmMessageInfo, SyncAlarmFileRequest localStorage) {
        localStorage.markAmsAlarmPrepared(
                oasAlarmMessageInfo.getAckSyncAlarmFileResult().getFileNames(),
                oasAlarmMessageInfo.getAckSyncAlarmFileResult().getServerIp());
    }

}
