package com.x.hhqs.joa.alarm.listener.kafka.processor;

import com.x.hhqs.joa.alarm.entity.JoaAlarm;
import com.x.hhqs.joa.alarm.model.MessageType;
import com.x.hhqs.joa.alarm.model.OmcRequest;
import com.x.hhqs.joa.alarm.model.response.OmcAckMessage;
import com.x.hhqs.joa.alarm.property.Messages;
import com.x.hhqs.joa.alarm.service.FileUploadService;
import com.x.hhqs.joa.alarm.service.SyncActiveAlarmFileService;
import com.x.hhqs.joa.common.core.contants.MessageConstant;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class ActiveAlarmProcessor {

    @Resource
    private SyncActiveAlarmFileService syncActiveAlarmFileService;

    @Resource
    private FileUploadService fileUploadService;

    public void processWriteAlarm(int channelIndex, String reqSyncId,
                             Channel channel,
                             OmcRequest omcRequest,
                             List<String> fileNames, String remoteIp,
                             String fileNamePre,
                             List<JoaAlarm> joaAlarms) throws Exception {
        String reqId = omcRequest.getValue("reqId");
        List<String> filePaths = new ArrayList<>();
        List<String> zipFilePaths = new ArrayList<>();
        try {
            SyncActiveAlarmFileService.FileWriteContext fileWriteContext = writeAlarmFiles(channelIndex, fileNames, reqSyncId, remoteIp, fileNamePre, joaAlarms);
            filePaths = fileWriteContext.getFinalFiles();

            // 3. compress
            zipFilePaths = compressFiles(filePaths);

            // 3. upload file to file server
            List<String> ftpPaths = uploadFiles(zipFilePaths, fileWriteContext.getRemotePath());

            // 4. notify client
            sendSuccessResponse(channel, reqId, ftpPaths);

            // delete ftp files
            try {
                for (String filePath : filePaths) {
                    String fileName = Paths.get(filePath).getFileName().toString();
                    fileUploadService.delete(fileWriteContext.getRemotePath(), fileName);
                }
            }catch (Exception e) {
                log.info("ftp files delete error!");
            }
        } catch (Exception e) {
            log.error("Alarm processing failed for reqId: {}", reqId, e);
            throw e;
        } finally {
            cleanupResources(filePaths, zipFilePaths);
        }
    }

    private SyncActiveAlarmFileService.FileWriteContext writeAlarmFiles(int channelIndex,
                                                                        List<String> fileNames,
                                                                        String reqSyncId,
                                                                        String remoteIp,
                                                                        String fileNamePre,
                                                                        List<JoaAlarm> joaAlarms
    ) throws Exception {
        SyncActiveAlarmFileService.FileWriteContext context = syncActiveAlarmFileService.initializeFileWriter(fileNamePre);
        try {
            log.info("start query active alarms, reqId[{}]", reqSyncId);
            syncActiveAlarmFileService.writeActiveAlarms(
                    fileNames,
                    context,
                    remoteIp,
                    channelIndex,
                    joaAlarms);
            List<String> finalFileNames = syncActiveAlarmFileService.finalizeWriting(context);
            context.setFinalFiles(finalFileNames);
        } catch (Exception e) {
            cleanupResources(syncActiveAlarmFileService.finalizeWriting(context), List.of());
            throw e;
        }
        return context;
    }

    private List<String> compressFiles(List<String> filePaths) throws Exception {
        return syncActiveAlarmFileService.zipFiles(filePaths);
    }

    private List<String> uploadFiles(List<String> zipFilePaths, String remotePath) throws Exception {
        List<String> ftpPaths = new ArrayList<>();
        for (String zipPath : zipFilePaths) {
            fileUploadService.uploadFile(remotePath, zipPath);
            String fileName = Paths.get(zipPath).getFileName().toString();
            ftpPaths.add(fileUploadService.getFullRemotePath(remotePath) + File.separator + fileName);
        }
        return ftpPaths;
    }

    private void sendSuccessResponse(Channel channel, String reqId, List<String> ftpPaths) {
        String ftpPathsStr = String.join(",", ftpPaths);
        log.info("{} async active alarms success!,response ftpPaths size-{}", reqId, ftpPathsStr);
        OmcAckMessage ack = OmcAckMessage.getAckMessage(MessageType.ackSyncAlarmFileResult);
        ack.setValue("reqId", reqId);
        ack.setValue("result", MessageConstant.SUCC);
        ack.setValue("filename", ftpPathsStr);
        channel.writeAndFlush(ack);
    }

    private void cleanupResources(List<String> filePaths, List<String> zipFilePaths) {
        List<String> allPaths = new ArrayList<>();
        if (filePaths != null) allPaths.addAll(filePaths);
        if (zipFilePaths != null) allPaths.addAll(zipFilePaths);
        syncActiveAlarmFileService.cleanupTempFiles(allPaths);
    }

  /*  public void sendErrorResponse(Channel channel, String reqId,String message) {
        log.error("{} async active alarms error!,response message {}", reqId, message);
        OmcAckMessage ack = OmcAckMessage.getAckMessage(MessageType.ackSyncAlarmFileResult);
        ack.setValue("reqId", reqId);
        ack.setValue("result", MessageConstant.FAIL);
        ack.setValue("resDesc", Messages.getString("resDesc.executeFail"));
        channel.writeAndFlush(ack);
    }*/
}