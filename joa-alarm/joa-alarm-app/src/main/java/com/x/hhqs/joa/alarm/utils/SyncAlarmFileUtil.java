package com.x.hhqs.joa.alarm.utils;

import cn.hutool.core.collection.CollectionUtil;
import com.x.hhqs.joa.alarm.config.JoaAlarmConfig;
import com.x.hhqs.joa.alarm.service.FileUploadService;
import com.x.hhqs.joa.alarm.vo.OmcAlarmVO;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
@Slf4j
public class SyncAlarmFileUtil {

    private static final String tempDir = System.getProperty("java.io.tmpdir");

    private static final String newLine = "\r\n";

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private JoaAlarmConfig joaAlarmConfig;

    @Getter
    private long maxFileSize;

    // default 100MB
    @Value("${joa-alarm.max-alarm-file-size: 100}")
    public void setMaxFileSize(long maxFileSize) {
        log.info("Max SyncAlarmFile size(MB): {}", maxFileSize);
        this.maxFileSize = maxFileSize * 1024 * 1024;
    }

    private String getCurrentDate() {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        return format.format(new Date());
    }

    public List<String> writeAlarmsToFiles(List<OmcAlarmVO> alarms, String reqSyncId) throws Exception {
        String remotePath = getCurrentDate();

        List<String> result = new ArrayList<>();
        SyncAlarmFile targetFile = new SyncAlarmFile(reqSyncId,joaAlarmConfig.getVersion());

        if (alarms == null || alarms.isEmpty()) {
            File file = new File(tempDir, targetFile.getName());
            file.createNewFile();
            result.add(file.getCanonicalPath());

            return result;
        }

        List<SyncAlarmFile> tmpResult = new ArrayList<>();
        tmpResult.add(targetFile);

        File tmpFile = new File(tempDir, targetFile.getTmpName());
        tmpFile.createNewFile();

        // upload tmp file (size=0)
        uploadFile(remotePath, tmpFile.getAbsolutePath());

        long fileSize = 0;

        // new FileOutputStream(tmpFile, true), 'append' not work, always true even if set false
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(tmpFile, true), StandardCharsets.UTF_8));
        for (OmcAlarmVO alarm : alarms) {
            fileSize = fileSize + alarm.getJsonValue().length() + 2;

            if (fileSize > getMaxFileSize()) {
                // 1. max size limit exceeded, close writer
                writer.close();

                // upload tmp file (size = max)
                uploadFile(remotePath, tmpFile.getAbsolutePath());

                // 2. if is first file, rename it to add '-001'
                if (targetFile.getIndex() == 0) {
                    targetFile.setIndex(1);

                    File newFile = new File(tempDir, targetFile.getName() + ".tmp");
                    String oldFileName = tmpFile.getName();
                    tmpFile.renameTo(newFile);

                    // ftp rename
                    rename(remotePath, oldFileName, newFile.getName());
                }

                // 3. next file
                targetFile = targetFile.getNext();
                deleteFileIfExist(targetFile.getTmpName());

                tmpFile = new File(tempDir, targetFile.getTmpName());
                tmpFile.createNewFile();

                // upload tmp file (size=0)
                uploadFile(remotePath, tmpFile.getAbsolutePath());

                // 4. add to tmp list
                tmpResult.add(targetFile);

                // 5. write to next file
                writer = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(tmpFile, true), StandardCharsets.UTF_8));

                // 6. reset fileSize
                fileSize = alarm.getJsonValue().length() + 2;
            }

            writer.write(alarm.getJsonValue());
            writer.write(newLine);
        }

        writer.flush();
        writer.close();

        // upload tmp file (finished)
        uploadFile(remotePath, tmpFile.getAbsolutePath());

        TimeUnit.SECONDS.sleep(3);

        // rename file to remove ".tmp"
        for (SyncAlarmFile syncAlarmFile : tmpResult) {
            File tmpAlarmFile = new File(tempDir, syncAlarmFile.getTmpName());
            File alarmFile = new File(tempDir, syncAlarmFile.getName());

            tmpAlarmFile.renameTo(alarmFile);
            result.add(alarmFile.getCanonicalPath());

            // rename ftp
            rename(remotePath, tmpAlarmFile.getName(), alarmFile.getName());
        }

        return result;
    }

    private void uploadFile(String remotePath, String filePath) throws Exception {
        fileUploadService.uploadFile(remotePath, filePath);
    }

    private void rename(String remotePath, String oldFileName, String newFileName) throws Exception {
        fileUploadService.rename(remotePath, oldFileName, newFileName);
    }

    private static void deleteFileIfExist(String fileName) throws IOException {
        File file = new File(tempDir, fileName);
        log.warn("Delete file:{}", file.getCanonicalPath());
        if (file.exists()) {
            log.warn("Delete file:{}", file.getCanonicalPath());
            file.delete();
        }
    }

    public static List<String> zipFiles(List<String> filePaths) throws Exception {
        List<String> result = new ArrayList<>();

        if (filePaths == null || filePaths.isEmpty())
            return result;

        for (String filePath : filePaths) {
            result.add(zipFile(filePath));
        }

        return result;
    }

    public static String zipFile(String filePath) throws Exception {
        File inputFile = new File(filePath);
        String zipFilePath = filePath + ".zip";

        FileOutputStream fileOutputStream = new FileOutputStream(zipFilePath);
        ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);

        ZipEntry zipEntry = new ZipEntry(inputFile.getName());
        zipOutputStream.putNextEntry(zipEntry);

        try (FileInputStream fileInputStream = new FileInputStream(inputFile)) {
            byte[] buf = new byte[1024];
            int bytesRead;

            while ((bytesRead = fileInputStream.read(buf)) > 0) {
                zipOutputStream.write(buf, 0, bytesRead);
            }
        }

        zipOutputStream.closeEntry();

        zipOutputStream.close();
        fileOutputStream.close();

        return zipFilePath;

    }

    public static String join(List<String> list, String delim) {
        return list == null || list.isEmpty() ? null : CollectionUtil.join(list, delim);
/*
      if (list == null || list.isEmpty())
            return null;
        StringBuilder sb = new StringBuilder();
        Iterator<String> iter = list.iterator();
        if (iter.hasNext())
            sb.append(iter.next());
        while (iter.hasNext()) {
            sb.append(delim);
            sb.append(iter.next());
        }
        return sb.toString();*/
    }

}

