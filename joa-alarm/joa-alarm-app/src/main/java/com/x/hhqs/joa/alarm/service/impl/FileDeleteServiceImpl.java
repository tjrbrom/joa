package com.x.hhqs.joa.alarm.service.impl;

import com.x.hhqs.joa.alarm.file.DestinationNode;
import com.x.hhqs.joa.alarm.file.FileDeleter;
import com.x.hhqs.joa.alarm.file.NodeType;
import com.x.hhqs.joa.alarm.file.deleter.SftpFileDeleter;
import com.x.hhqs.joa.alarm.service.FileDeleteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class FileDeleteServiceImpl implements FileDeleteService {

    @Override
    public void deleteFilesByAliveTime(DestinationNode node, long aliveTime, TimeUnit timeUnit) throws Exception {
        log.info("Delete all files in '{}', {} {} ago.", getFullUriPath(node), aliveTime, timeUnit);
        try (FileDeleter deleter = getDeleter(node)) {
            deleter.deleteFilesByAliveTime(aliveTime, timeUnit);
        }
    }

    @Override
    public void deleteFilesByQuantityLimit(DestinationNode node, int maxQuantity) throws Exception{
        log.info("Delete all files except {} newest ones in '{}'", maxQuantity, getFullUriPath(node));
        try (FileDeleter deleter = getDeleter(node)) {
            deleter.deleteFilesByQuantityLimit(maxQuantity);
        }
    }

    public FileDeleter getDeleter(DestinationNode node) {
        if (node.getType() == NodeType.SFTP) {
            return new SftpFileDeleter(node);
        }
        throw new IllegalArgumentException("Unsupported type: " + node.getType());
    }

    private static String getFullUriPath(DestinationNode node) {
        return node.getUploadPath();
    }
}
