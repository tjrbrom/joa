package com.x.hhqs.joa.alarm.file.uploader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;



import com.x.hhqs.joa.alarm.file.DestinationNode;
import com.x.hhqs.joa.alarm.file.FileUploader;
import lombok.extern.slf4j.Slf4j;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

@Slf4j
public class SftpFileUploader implements FileUploader {

    private final DestinationNode node;

    private final JSch sshClient;

    private Session session;

    private ChannelSftp sftp;

    public SftpFileUploader(DestinationNode node) {
        this.node = node;
        this.sshClient = new JSch();
    }

    public SftpFileUploader(DestinationNode node, JSch jSch) {
        this.node = node;
        this.sshClient = jSch;
    }

    @Override
    public void close() {
        if (session != null) {
            session.disconnect();
        }
        if (sftp != null) {
            sftp.disconnect();
        }
    }

    @Override
    public void uploadFile(String remotePath, String sourceFilePath) throws Exception {
        Path toPath = Paths.get(node.getPath(), remotePath);
        Path fromPath = Paths.get(sourceFilePath);

        uploadFileWithName(toPath, fromPath, fromPath.getFileName().toString());
    }

    private void uploadFileWithName(Path toPath, Path fromPath, String fileName) throws Exception {
        if (!Files.exists(fromPath) || !fromPath.toFile().isFile()) {
            throw new IOException(String.format("%s is not a file", fromPath));
        }

        // check connection, reconnect if needed
        if (checkConnection()) {
            reconnect();
        }

        // create directory if needed
        mkdirIfDirectoryNotExist(toPath.toString());

        // upload
        sftp.put(fromPath.toString(), toPath.resolve(fileName).toString(), ChannelSftp.OVERWRITE);

    }

    private void mkdirIfDirectoryNotExist(String path) throws IOException {
        try {
            sftp.cd("/");
            String[] folders = path.split("/");
            for (String folder : folders) {
                if (!folder.isEmpty()) {
                    try {
                        sftp.cd(folder);
                    } catch (SftpException e) {
                        sftp.mkdir(folder);
                        sftp.cd(folder);
                    }
                }
            }
        } catch (Exception e) {
            log.error("sftp mkdir directory  error",e);
            throw new IOException(e.getMessage());
        }
    }

    private boolean checkConnection() {
        return sftp == null || !sftp.isConnected();
    }

    private void reconnect() throws Exception {
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");

        // session
        session = sshClient.getSession(node.getUser(), node.getIp(), node.getPort());
        session.setConfig(config);
        session.setPassword(node.getPassword());
        session.connect();

        // ChannelSftp
        sftp = (ChannelSftp) session.openChannel("sftp");
        sftp.connect();
    }

    @Override
    public void rename(String remotePath, String oldFileName, String newFileName) {
        try {
            log.info("sftp rename remote fileName: newFileName{} rename to {}", oldFileName, oldFileName);
            if (checkConnection()) {
                reconnect();
            }
            Path form = Paths.get(node.getPath(), remotePath, oldFileName);
            Path to = Paths.get(node.getPath(), remotePath, newFileName);
            sftp.rename(form.toString(), to.toString());
        } catch (Exception e) {
            log.error("sftp rename remote fileName error!",e);
        }

    }

    @Override
    public void delete(String remotePath, String fileName) {
        try {
            log.info("sftp delete remote file:{}/{}", remotePath, fileName);
            if (checkConnection()) {
                reconnect();
            }
            Path path = Paths.get(node.getPath(), remotePath, fileName).normalize();
            sftp.rm(path.toString());
        } catch (Exception e) {
            log.error("sftp delete remote file error! ", e);
        }
    }

}
