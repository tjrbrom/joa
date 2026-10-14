package com.x.hhqs.joa.alarm.service.impl;

import com.x.hhqs.joa.alarm.file.DestinationNode;
import com.x.hhqs.joa.alarm.file.FileUploader;
import com.x.hhqs.joa.alarm.file.FileUploaderManager;
import com.x.hhqs.joa.alarm.service.FileUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


@Slf4j
@Component
public class FileUploadServiceImpl implements FileUploadService {

    @Resource
    private DestinationNode destinationNode;

    public FileUploadServiceImpl() {
        super();
    }

    @Override
    public void uploadFile(String remotePath, String sourceFilePath) throws Exception {
        log.info("Upload file {} to path: {}", sourceFilePath, getFullRemotePath(remotePath));
        FileUploader uploader = getFileUpLoader();
        uploader.uploadFile(remotePath, sourceFilePath);
    }

    @Override
    public String getFullRemotePath(String remotePath) {
        String uploadPath = destinationNode.getUploadPath();

        if (remotePath.startsWith("/")) {
            return uploadPath + remotePath;
        }

        return uploadPath + "/" + remotePath;
    }

    @Override
    public void rename(String remotePath, String oldFileName, String newFileName) throws Exception {
        try (FileUploader uploader = getFileUpLoader()) {
            uploader.rename(remotePath, oldFileName, newFileName);
        }
    }

    @Override
    public void delete(String remotePath, String fileName) throws Exception {
        try (FileUploader uploader = getFileUpLoader()) {
            uploader.delete(remotePath, fileName);
        }
    }

    public FileUploader getFileUpLoader() throws Exception {
        return FileUploaderManager.getFileUploader(destinationNode);
    }

}
