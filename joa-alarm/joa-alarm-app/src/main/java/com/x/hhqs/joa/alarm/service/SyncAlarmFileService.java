package com.x.hhqs.joa.alarm.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.x.hhqs.joa.alarm.config.JoaAlarmConfig;
import com.x.hhqs.joa.alarm.entity.JoaAlarm;
import com.x.hhqs.joa.alarm.mapper.JoaAlarmMapper;
import com.x.hhqs.joa.alarm.utils.SyncAlarmFile;
import com.x.hhqs.joa.alarm.utils.SyncAlarmFileUtil;
import com.x.hhqs.joa.alarm.vo.OmcAlarmVO;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cursor.Cursor;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Service
@Slf4j
public class SyncAlarmFileService {

    private static final String tempDir = System.getProperty("java.io.tmpdir");

    private static final String newLine = "\r\n";

    private static final long FILE_SIZE_UNIT_MB = 1024 * 1024;

    @Resource
    private JoaAlarmConfig joaAlarmConfig;

    @Resource
    private JoaAlarmMapper joaAlarmMapper;

    @Autowired
    private FileUploadService fileUploadService;

    @Transactional(timeout = 30)
    public List<String> queryAlarmsFilePathStartWithId(String fileNamePre, Long id, int channelIndex) throws Exception {
        QueryWrapper<JoaAlarm> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("channel_index", channelIndex)
                .ge("id", id);
        return processAlarmsAanWriteAlarmsToFiles(fileNamePre, queryWrapper);
    }

    @Transactional(timeout = 30)
    public List<String> queryAlarmsFilePathByDuringTime(String fileNamePre, Date startTime, Date endTime, int channelIndex) throws Exception {
        QueryWrapper<JoaAlarm> joaAlarmQueryWrapper = new QueryWrapper<>();
        joaAlarmQueryWrapper.eq("channel_index", channelIndex);
        joaAlarmQueryWrapper.ge(startTime != null, "create_time", startTime);
        joaAlarmQueryWrapper.le("create_time",endTime == null ? new Date() : endTime);
        return processAlarmsAanWriteAlarmsToFiles(fileNamePre, joaAlarmQueryWrapper);
    }

    public List<String> processAlarmsAanWriteAlarmsToFiles(String fileNamePre, QueryWrapper<JoaAlarm> queryWrapper) throws Exception {
        List<String> result = new ArrayList<>();
        String remotePath = getCurrentDate();
        SyncAlarmFile targetFile = new SyncAlarmFile(fileNamePre, joaAlarmConfig.getVersion());
        List<SyncAlarmFile> tmpFiles = new ArrayList<>();
        Cursor<OmcAlarmVO> cursor = null;
        BufferedWriter writer = null;
        try {
            cursor = joaAlarmMapper.selectCursor(queryWrapper);
            File tmpFile = null;
            long currentSize = 0;
            for (OmcAlarmVO omcAlarmVO : cursor) {
                String json = omcAlarmVO.getJsonValue();
                long recordSize = json.length() + 2;
                if (writer == null || currentSize + recordSize > joaAlarmConfig.getMaxAlarmFileSize()) {
                    if (writer != null) {
                        writer.close();
                        uploadAndRotateFile(remotePath, tmpFile, targetFile, tmpFiles);
                    }
                    targetFile = targetFile.getNext();
                    tmpFile = createTempFile(remotePath, targetFile);
                    writer = createBufferedWriter(tmpFile);
                    currentSize = 0;
                }
                writer.write(json);
                writer.write(newLine);
                currentSize += recordSize;
            }
            if (writer != null) {
                writer.close();
                uploadAndRotateFile(remotePath, tmpFile, targetFile, tmpFiles);
            }
            if (tmpFiles.isEmpty()) {
                targetFile = targetFile.getNext();
                tmpFile = createTempFile(remotePath, targetFile);
                writer = createBufferedWriter(tmpFile);
                uploadAndRotateFile(remotePath, tmpFile, targetFile, tmpFiles);
            }
        } finally {
            if (cursor != null && cursor.isOpen()) {
                cursor.close();
            }
            if (writer != null) {
                writer.close();
            }
        }
        renameTempFiles(remotePath, tmpFiles, result);
        return result;
    }

    private String getCurrentDate() {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        return format.format(new Date());
    }

    private File createTempFile(String remotePath, SyncAlarmFile syncFile) throws Exception {
        Path path = Paths.get(tempDir, syncFile.getTmpName());
        File tmpFile = Files.write(path, new byte[0], StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING).toFile();
        fileUploadService.uploadFile(remotePath, tmpFile.getAbsolutePath());
        return tmpFile;
    }

    private BufferedWriter createBufferedWriter(File file) throws Exception {
        return new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file, true),
                        joaAlarmConfig.getCharset()));
    }

    private void uploadAndRotateFile(String remotePath, File tmpFile, SyncAlarmFile meta, List<SyncAlarmFile> tmpFiles) throws Exception {
        fileUploadService.uploadFile(remotePath, tmpFile.getAbsolutePath());
        if (meta.getIndex() == 0) {
            meta.setIndex(1);
            Path src = Paths.get(tempDir, meta.getName());
            Path dst = Paths.get(tempDir, meta.getName() + ".tmp");
            Files.move(src, dst,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
            fileUploadService.rename(remotePath, tmpFile.getName(), dst.toFile().getName());
        }
        tmpFiles.add(meta);
    }

    private void renameTempFiles(String remotePath, List<SyncAlarmFile> tmpFiles, List<String> result) throws Exception {
        for (SyncAlarmFile meta : tmpFiles) {
            Path src = Paths.get(tempDir, meta.getTmpName());
            Path dst = Paths.get(tempDir, meta.getName());
            Files.move(src, dst,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
            fileUploadService.rename(remotePath, src.toFile().getName(), dst.toFile().getName());
            result.add(dst.toFile().getCanonicalPath());
        }
    }

    public List<String> zipFiles(List<String> filePaths) throws Exception {
        return SyncAlarmFileUtil.zipFiles(filePaths);
    }
}

