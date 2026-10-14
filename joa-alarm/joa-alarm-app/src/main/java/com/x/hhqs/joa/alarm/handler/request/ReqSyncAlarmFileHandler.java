package com.x.hhqs.joa.alarm.handler.request;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.x.hhqs.joa.alarm.config.JoaAlarmConfig;
import com.x.hhqs.joa.alarm.dto.OASAlarmMessageInfo;
import com.x.hhqs.joa.alarm.entity.JoaAlarm;
import com.x.hhqs.joa.alarm.handler.RequestMessageHandler;
import com.x.hhqs.joa.alarm.listener.kafka.processor.ActiveAlarmProcessor;
import com.x.hhqs.joa.alarm.listener.kafka.processor.AsyncActiveAlarmMessageProcessor;
import com.x.hhqs.joa.alarm.model.*;
import com.x.hhqs.joa.alarm.model.ams.*;
import com.x.hhqs.joa.alarm.model.response.OmcAckMessage;
import com.x.hhqs.joa.alarm.property.Messages;
import com.x.hhqs.joa.alarm.server.ChannelUtil;
import com.x.hhqs.joa.alarm.service.*;

import com.x.hhqs.joa.alarm.service.manager.SyncAlarmFileChannelManage;
import com.x.hhqs.joa.alarm.utils.DateTimeUtils;
import com.x.hhqs.joa.alarm.utils.SyncAlarmFileUtil;
import com.x.hhqs.joa.common.core.contants.MessageConstant;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.net.InetSocketAddress;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


@Component
@Slf4j
public class ReqSyncAlarmFileHandler extends RequestMessageHandler {

    private final SyncAlarmFileTask invalidSyncSourceTask = new InvalidSyncSourceTask();

    @Autowired
    private OmcAlarmQueryService omcAlarmQueryService;

    @Autowired
    private AsyncTaskManager asyncTaskManager;

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private JoaAlarmConfig joaAlarmConfig;

    @Autowired
    private SyncAlarmFileService syncAlarmFileService;

    @Resource
    private KafkaTemplate<String, AmsAlarmMessage> kafkaTemplate;

    @Resource
    private AsyncActiveAlarmMessageProcessor asyncActiveAlarmMessageProcessor;

    @Resource
    private SyncAlarmFileChannelManage syncAlarmFileChannelManage;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private SyncActiveAlarmFileService syncActiveAlarmFileService;

    @Resource
    private ActiveAlarmProcessor activeAlarmProcessor;

    /*-
    reqSyncAlarmFile;reqId=33;startTime=2014-11-27 10:00:00;endTime=2024-11-27 10:30:00; syncSource =0
    reqSyncAlarmFile;reqId=34;startTime=2014-11-27 10:00:00;endTime=2014-11-27 10:30:00; syncSource =1
    reqSyncAlarmFile;reqId=35;alarmSeq=1; syncSource =1
    */
    @Override
    public void executeRequest(Channel ch, OmcRequest request) {
        String reqId = request.getValue("reqId");

        // 1.check clientType is ClientType.ftp
        ClientSession session = ChannelUtil.getSession(ch);
        ClientType clientType = session.getClientType();
        if (clientType != ClientType.ftp) {
            log.error("ReqSyncAlarmFile need execute with login type: ftp.");
            fail(ch, reqId, Messages.getString("resDesc.wrongLoginType"));
            return;
        }
        String reqSyncId = isWaitingForSyncAlarmResult(ch, reqId, session);
        if (StringUtils.isEmpty(reqSyncId)) {
            return;
        }
        // 2. create asynchronous task
        SyncAlarmFileTask task = createSyncAlarmFileTask(request, ch);

        // 3. validate parameters
        OperateResult result = task.validateParameters();
        if (!result.isSuccess()) {
            String errorMessage = result.getErrorMessage();
            fail(ch, reqId, errorMessage);
            return;
        }
        syncAlarmFileChannelManage.saveRequestToStorage(reqSyncId, new SyncAlarmFileRequest(
                session.getUserName(),
                reqSyncId,
                ch,
                request,
                joaAlarmConfig.getServer().getPort(),
                Instant.now().toEpochMilli()
        ));

        // 4. schedule task
        asyncTaskManager.execute(task);

        // 5. notify client
        success(ch, reqId);
    }

    private int getChannelIndex(Channel ch) {
        int port = ((InetSocketAddress) ch.localAddress()).getPort();
        return port - joaAlarmConfig.getServer().getPort();
    }

    private void success(Channel ch, String reqId) {
        OmcAckMessage ackMessage = getOmcAckMessage();
        ackMessage.setValue("reqId", reqId);
        ackMessage.setValue("result", MessageConstant.SUCC);

        ch.writeAndFlush(ackMessage);
    }

    private void fail(Channel ch, String reqId, String resDesc) {
        OmcAckMessage ackMessage = getOmcAckMessage();
        ackMessage.setValue("reqId", reqId);
        ackMessage.setValue("result", MessageConstant.FAIL);
        ackMessage.setValue("resDesc", resDesc);

        ch.writeAndFlush(ackMessage);

    }

    private SyncAlarmFileTask createSyncAlarmFileTask(OmcRequest request, Channel ch) {
        String syncSource = request.getValue("syncSource");
        if ("0".equals(syncSource)) {
            return new SyncAmsAlarmFileTask(request, ch);
        } else if ("1".equals(syncSource)) {
            return new SyncOmcAlarmFileTask(request, ch);
        } else {
            return invalidSyncSourceTask;
        }

    }

    @Override
    public MessageType getType() {
        return MessageType.reqSyncAlarmFile;
    }

    private boolean isValidTimeValue(String time) {
        if (time != null && !time.isEmpty()) {
            return DateTimeUtils.parseDateTimeStrGet(time) == null;
        }
        return false;
    }


    interface SyncAlarmFileTask extends Runnable {
        OperateResult validateParameters();
    }

    class SyncOmcAlarmFileTask implements SyncAlarmFileTask {

        private boolean queryByTime = false;

        private final OmcRequest request;

        private final Channel ch;

        public SyncOmcAlarmFileTask(boolean queryByTime, OmcRequest request, Channel ch) {
            this.request = request;
            this.ch = ch;
            this.queryByTime = queryByTime;
        }

        public SyncOmcAlarmFileTask(OmcRequest request, Channel ch) {
            this.request = request;
            this.ch = ch;
        }

        @Override
        public void run() {
            String reqId = request.getValue("reqId");
            List<String> filePaths = new ArrayList<>();
            List<String> zipFilePaths = new ArrayList<>();
            String user = null;
            String reqSyncId = "";
            try {
                user = ChannelUtil.getSession(ch).getUserName();
                int channelIndex = getChannelIndex(ch);
                // 1. query alarm list
                reqSyncId = String.format("%s_%s", user , ((InetSocketAddress) ch.localAddress()).getPort());
                String fileNamePre = String.format("%s_%s_%s", user , channelIndex, reqId);
                if (queryByTime) {
                    Date startTime = ObjectUtils.isEmpty(request.getValue("startTime")) ? null : DateTimeUtils.parseDate(request.getValue("startTime"));
                    Date endTime =  ObjectUtils.isEmpty(request.getValue("endTime")) ? null :DateTimeUtils.parseDate(request.getValue("endTime"));
                    filePaths = syncAlarmFileService.queryAlarmsFilePathByDuringTime(fileNamePre, startTime, endTime, channelIndex);
                } else {
                    Long alarmSeq = request.getLongValue("alarmSeq");
                    JoaAlarm alarm = omcAlarmQueryService.getOmcAlarmByAlarmSeq(alarmSeq, channelIndex);

                    if (alarm == null) {
                        log.warn("alarmSeq doesn't exist, alarmSeq={}", alarmSeq);
                        // if alarm with alarmSeq does not exist, ignore this
                        // sync
                        OmcAckMessage ackMessage = OmcAckMessage.getAckMessage(MessageType.ackSyncAlarmFileResult);
                        ackMessage.setValue("reqId", reqId);
                        ackMessage.setValue("result", MessageConstant.FAIL);
                        ackMessage.setValue("resDesc", Messages.getString("resDesc.alarmSeqNotExist"));

                        ch.writeAndFlush(ackMessage);
                        syncAlarmFileChannelManage.removeRequestFromStorage(reqSyncId);
                        return;
                    }
                    filePaths = syncAlarmFileService.queryAlarmsFilePathStartWithId(fileNamePre, alarm.getId(), channelIndex);
                }

                // 2. write result list to temporary file
//                List<String> filePaths = syncAlarmFileUtil.writeAlarmsToFiles(alarms, reqSyncId);
//                List<String> filePaths = syncAlarmFileUtil.writeAlarmsToFiles(alarms, reqSyncId);

                // 3. compress
                zipFilePaths = syncAlarmFileService.zipFiles(filePaths);

                // 3. upload file to file server
                List<String> ftpFilePaths = new ArrayList<>();
                String remotePath = getCurrentDate();
                String fullRemotePath = fileUploadService.getFullRemotePath(remotePath);
                for (String zipFilePath : zipFilePaths) {
                    fileUploadService.uploadFile(remotePath, zipFilePath);

                    String fileName = Paths.get(zipFilePath).getFileName().toString();
                    ftpFilePaths.add(fullRemotePath + File.separator + fileName);
                }

                // delete ftp files
                for (String filePath : filePaths) {
                    String fileName = Paths.get(filePath).getFileName().toString();
                    fileUploadService.delete(remotePath, fileName);
                }

                // 4. notify client
                OmcAckMessage ackMessage = OmcAckMessage.getAckMessage(MessageType.ackSyncAlarmFileResult);
                ackMessage.setValue("reqId", reqId);
                ackMessage.setValue("result", MessageConstant.SUCC);
                ackMessage.setValue("filename", SyncAlarmFileUtil.join(ftpFilePaths, ","));

                ch.writeAndFlush(ackMessage);

            } catch (Exception e) {
                log.error("", e);
                // notify client
                OmcAckMessage ackMessage = OmcAckMessage.getAckMessage(MessageType.ackSyncAlarmFileResult);
                ackMessage.setValue("reqId", reqId);
                ackMessage.setValue("result", MessageConstant.FAIL);
                ackMessage.setValue("resDesc", Messages.getString("resDesc.executeFail"));
                ch.writeAndFlush(ackMessage);
            } finally {
                // 5. delete temporary file
                log.info("delete local temporary file");
                deleteFileIfExist(filePaths);
                deleteFileIfExist(zipFilePaths);
                syncAlarmFileChannelManage.removeRequestFromStorage(reqSyncId);
            }
        }

        @Override
        public OperateResult validateParameters() {
            String alarmSeq = request.getValue("alarmSeq");
            if (alarmSeq == null) {
                queryByTime = true;

                // check startTime
                if (isValidTimeValue(request.getValue("startTime"))) {
                    return OperateResult.fail(invalidParamter("startTime"));
                }

                if (isValidTimeValue(request.getValue("endTime"))) {
                    return OperateResult.fail(invalidParamter("endTime"));
                }

            } else {
                // check alarmSeq
                if (!StringUtils.isNumeric(alarmSeq)) {
                    return OperateResult.fail(invalidParamter("alarmSeq"));
                }
            }

            return OperateResult.SUCCESS;
        }

    }

    class SyncAmsAlarmFileTask implements SyncAlarmFileTask {

        private final OmcRequest request;

        private final Channel ch;

        public SyncAmsAlarmFileTask(OmcRequest request, Channel ch) {
            this.request = request;
            this.ch = ch;
        }

        @Override
        public void run() {
            String reqId = request.getValue("reqId");
            log.info("asyncAmsAlarmFile start!");
            String reqSyncId = "";
            try {
                ClientSession session = ChannelUtil.getSession(ch);
                reqSyncId = String.format("%s_%s", session.getUserName() , ((InetSocketAddress) ch.localAddress()).getPort());
                SyncAlarmFileRequest syncAlarmFileRequest = syncAlarmFileChannelManage.getSyncAlarmFileRequest(reqSyncId);
                String fileNamePre = String.format("%s_%s_%s", session.getUserName() , syncAlarmFileRequest.getChannelIndex(), reqId);
                if (joaAlarmConfig.getAmsServerIsStart()) {
                    log.info("{} async es and ams active alarm data", reqSyncId);
                    AmsAlarmMessage amsAlarmMessage = new AmsAlarmMessage();
                    amsAlarmMessage.setReqId(syncAlarmFileRequest.getReqId());
                    String startTime = request.getValue("startTime");
                    String endTime = request.getValue("endTime");
                    amsAlarmMessage.setStartTime(startTime);
                    amsAlarmMessage.setEndTime(endTime);
                    amsAlarmMessage.setUserName(null);
                    kafkaTemplate.send("OAS_ALARM_REQ", amsAlarmMessage);
                    log.info("send message to [OAS_ALARM_REQ] success!");
                    final CountDownLatch latch = new CountDownLatch(1);
                    syncAlarmFileRequest.setCompletionListener(latch::countDown);
                    List<JoaAlarm> joaAlarms = new ArrayList<>();
                    try {
                        final LocalDateTime sTime = ObjectUtils.isEmpty(startTime) ? null : DateTimeUtils.parseDateTimeStrGet(startTime);
                        final LocalDateTime eTime = ObjectUtils.isEmpty(endTime) ? LocalDateTime.now() : DateTimeUtils.parseDateTimeStrGet(endTime);
                        joaAlarms = syncActiveAlarmFileService.queryESActiveAlarms(sTime, eTime, syncAlarmFileRequest.getChannelIndex());
                        syncAlarmFileRequest.markCoreAlarmPrepared(true);
                    } catch (Exception e) {
                        syncAlarmFileRequest.markCoreAlarmPrepared(false);
                        log.warn("handle core alarm fail", e);
                    }
                    if (!latch.await(joaAlarmConfig.getAmsTimeout(), TimeUnit.SECONDS) && !syncAlarmFileRequest.getCoreAlarmSucc()) {
                        throw new TimeoutException("Dual condition timeout");
                    }
                    syncAlarmFileRequest = syncAlarmFileChannelManage.getSyncAlarmFileRequest(syncAlarmFileRequest.getReqSyncId());
                    if (syncAlarmFileRequest.getCoreAlarmSucc()) {
                        activeAlarmProcessor.processWriteAlarm(syncAlarmFileRequest.getChannelIndex(),
                                reqSyncId,
                                ch,
                                request,
                                syncAlarmFileRequest.getAmsFileNames(),
                                syncAlarmFileRequest.getRemoteIP(),
                                fileNamePre,
                                joaAlarms
                        );
                    } else {
                        throw new Exception("async es active alarm data error");
                    }
                } else {
                    log.info("async es active alarm data");
                    List<JoaAlarm> joaAlarms = processCoreAlarm(request, syncAlarmFileRequest.getChannelIndex());
                    activeAlarmProcessor.processWriteAlarm(syncAlarmFileRequest.getChannelIndex(),
                            reqSyncId,
                            ch,
                            request,
                            List.of(),
                            null,
                            fileNamePre,
                            joaAlarms
                    );
                }
            } catch (Exception e) {
                log.error("SyncAmsAlarmFileTask run error", e);
                OmcAckMessage ackMessage = OmcAckMessage.getAckMessage(MessageType.ackSyncAlarmFileResult);
                ackMessage.setValue("reqId", reqId);
                ackMessage.setValue("result", MessageConstant.FAIL);
                ackMessage.setValue("resDesc", Messages.getString("resDesc.executeFail"));
                ch.writeAndFlush(ackMessage);
            } finally {
                syncAlarmFileChannelManage.removeRequestFromStorage(reqSyncId);
            }
        }

        private List<JoaAlarm> processCoreAlarm(OmcRequest request, int channelIndex) throws Exception {
            String startTime = request.getValue("startTime");
            String endTime = request.getValue("endTime");
            final LocalDateTime sTime = ObjectUtils.isEmpty(startTime) ? null : DateTimeUtils.parseDateTimeStrGet(startTime);
            final LocalDateTime eTime = ObjectUtils.isEmpty(endTime) ? LocalDateTime.now() : DateTimeUtils.parseDateTimeStrGet(endTime);
            return syncActiveAlarmFileService.queryESActiveAlarms(sTime, eTime, channelIndex);
        }

        @Override
        public OperateResult validateParameters() {
            // check startTime
            if (isValidTimeValue(request.getValue("startTime"))) {
                return OperateResult.fail(invalidParamter("startTime"));
            }

            // check endTime
            if (isValidTimeValue(request.getValue("endTime"))) {
                return OperateResult.fail(invalidParamter("endTime"));
            }
            return OperateResult.SUCCESS;
        }
    }


    class InvalidSyncSourceTask implements SyncAlarmFileTask {

        @Override
        public void run() {
        }

        @Override
        public OperateResult validateParameters() {
            return OperateResult.fail(invalidParamter("syncSource"));
        }

    }

    private String getCurrentDate() {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        return format.format(new Date());
    }

    private static void deleteFileIfExist(List<String> filePaths) {
        filePaths.forEach(filePath -> {
            File file = new File(filePath);
            if (file.exists()) {
                log.warn("Delete file:{}", filePath);
                file.delete();
            }
        });
    }

    synchronized private String isWaitingForSyncAlarmResult(Channel ch, String reqId, ClientSession session) {
        String userName = session.getUserName();
        String reqSyncId = String.format("%s_%s", userName , ((InetSocketAddress) ch.localAddress()).getPort());
        if (syncAlarmFileChannelManage.isExistSyncAlarmFileRequest(userName,  ((InetSocketAddress) ch.localAddress()).getPort())){
            fail(ch, reqId, Messages.getString("resDesc.reqSyncAlarmFileRunning"));
            return null;
        }
        return reqSyncId;
    }

}
