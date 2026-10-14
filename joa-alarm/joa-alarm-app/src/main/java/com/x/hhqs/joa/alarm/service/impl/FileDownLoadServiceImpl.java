package com.x.hhqs.joa.alarm.service.impl;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.x.hhqs.joa.alarm.file.DestinationNode;
import com.x.hhqs.joa.alarm.service.FileDownLoadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@Slf4j
public class FileDownLoadServiceImpl implements FileDownLoadService {

    @Resource
    private DestinationNode destinationNode;

    private static final String tempDir = System.getProperty("java.io.tmpdir");

    private final JSch sshClient;

    public FileDownLoadServiceImpl() {
        this.sshClient = new JSch();
    }

    public FileDownLoadServiceImpl(JSch sshClient,DestinationNode destinationNode) {
        this.sshClient = sshClient;
        this.destinationNode = destinationNode;
    }

    @Override
    public String downloadFile(String remoteFilePath, String ip) {
        ChannelSftp channelSftp = null;
        try {
            log.info("sftp download {} to {}", remoteFilePath , tempDir);
            channelSftp = connect(ip);
            if (channelSftp.isConnected()) {
                String remoteFileName = remoteFilePath.substring(remoteFilePath.lastIndexOf('/') + 1);
                String localFilePath = tempDir + "/" + System.currentTimeMillis() + "_" + remoteFileName ;
                channelSftp.get(remoteFilePath, localFilePath);
                return localFilePath;
            } else {
                log.error("sftp connected error!");
            }
        } catch (Exception e) {
            log.error("sftp download remote fileName error!",e);
        } finally {
            if (channelSftp != null) {
                channelSftp.disconnect();
            }
        }
        return "";
    }

    private ChannelSftp connect(String remotIp) throws Exception {
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");

        // session
        Session session = sshClient.getSession(destinationNode.getUser(), remotIp, 22);
        session.setConfig(config);
        session.setPassword(destinationNode.getPassword());
        session.connect();

        // ChannelSftp
        ChannelSftp sftp = (ChannelSftp) session.openChannel("sftp");
        sftp.connect();
        return sftp;
    }
}
