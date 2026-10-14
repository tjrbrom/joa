package com.x.hhqs.joa.alarm.file;

import com.x.hhqs.joa.alarm.file.deleter.SftpFileDeleter;
import com.x.hhqs.joa.alarm.file.uploader.SftpFileUploader;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileUploaderManager {

    public static FileUploader getFileUploader(DestinationNode node) throws Exception {
        if (node == null) {
            throw new Exception("DestinationNode is invalid, please check config file.");
        }
        if (node.getType() == NodeType.SFTP) {
            return new SftpFileUploader(node);
        }
        throw new IllegalArgumentException("Unsupported type: " + node.getType());
    }
}
