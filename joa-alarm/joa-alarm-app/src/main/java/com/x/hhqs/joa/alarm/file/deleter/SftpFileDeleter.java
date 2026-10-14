package com.x.hhqs.joa.alarm.file.deleter;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.x.hhqs.joa.alarm.file.DestinationNode;
import com.x.hhqs.joa.alarm.file.FileDeleter;
import com.x.hhqs.joa.alarm.file.NodeType;
import com.x.hhqs.joa.alarm.file.utils.SftpFiles;
import com.x.hhqs.joa.alarm.file.utils.SimpleRemoteFileVisitor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SftpFileDeleter implements FileDeleter {

    private DestinationNode node;

    private JSch sshClient;

    private Session session;

    private ChannelSftp sftp;

    public SftpFileDeleter(DestinationNode node) {
        this.node = node;
        this.sshClient = new JSch();
    }

    public SftpFileDeleter(DestinationNode node, JSch sshClient) {
        this.node = node;
        this.sshClient = sshClient;
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
    public void deleteFilesByAliveTime(long aliveTime, TimeUnit unit) throws Exception {
        deleteFilesByAliveTime(aliveTime, unit, GLOBBING_ALL_FILES);
    }

    @Override
    public void deleteFilesByQuantityLimit(int maxQuantity) throws Exception {
        deleteFilesByQuantityLimit(maxQuantity, GLOBBING_ALL_FILES);
    }

    public void deleteFilesByQuantityLimit(int maxQuantity, String globbing) throws Exception {
        if (!checkConnection()) {
            reconnect();
        }

        final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + globbing);
//        Collection<ChannelSftp.LsEntry> files = sftp.ls(node.getPath());
        Collection<ChannelSftp.LsEntry> files = sftp.ls(node.getPath()).stream().toList();
        files.stream()
                .filter(f -> !(".".equals(f.getFilename()) || "..".equals(f.getFilename())))
                .filter(f -> matcher.matches(Paths.get(f.getFilename())))
                .sorted((f1, f2) -> f2.getAttrs().getMTime() - f1.getAttrs().getMTime())
                .skip(maxQuantity)
                .forEach(f -> delete(f, node.getPath()));
    }

    public void deleteFilesByAliveTime(long aliveTime, TimeUnit unit, String globbing) throws Exception {

        if (!checkConnection()) {
            reconnect();
        }

        final long aliveTimeInMillis = unit.toMillis(aliveTime);
        final long current = System.currentTimeMillis();

        final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**/" + globbing);

        SftpFiles.walkFileTree(sftp, node.getPath(), new SimpleRemoteFileVisitor<LsEntry>() {
            @Override
            public FileVisitResult visitFile(LsEntry file, String parentPath) throws IOException {
                if (!matcher.matches(Paths.get(parentPath, file.getFilename()))) {
                    return FileVisitResult.CONTINUE;
                }
                // delete expired file
                else if ((current - getMTimeInMillis(file)) > aliveTimeInMillis) {
                    delete(file, parentPath);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(LsEntry dir, String parentPath, IOException e)
                    throws IOException {
                if (e != null)
                    throw e;

                if (!matcher.matches(Paths.get(parentPath, dir.getFilename()))) {
                    return FileVisitResult.CONTINUE;
                }
                // delete expired directory if is empty
                else if ((current - getMTimeInMillis(dir)) > aliveTimeInMillis) {
                    deleteDirectoryIfEmpty(dir, parentPath);
                }

                return FileVisitResult.CONTINUE;
            }

        });
    }

    private boolean checkConnection() {
        return sftp != null && sftp.isConnected();
    }

    private void reconnect() throws Exception {
        Properties config = new Properties();
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


    private long getMTimeInMillis(ChannelSftp.LsEntry file) {
        return file.getAttrs().getMTime() * 1000L;
    }

    @SuppressWarnings("unchecked")
    private void deleteDirectoryIfEmpty(ChannelSftp.LsEntry dir, String parentPath) {
        try {
            String path = parentPath + "/" + dir.getFilename();
//            Collection<ChannelSftp.LsEntry> files = sftp.ls(path);
            Collection<ChannelSftp.LsEntry> files = sftp.ls(path).stream().toList();

            // '.' and '..'
            if (files.size() == 2 || files.isEmpty()) {
                log.info("delete empty directory: {}", path);
                sftp.rmdir(path);
            }

        } catch (Exception e) {
            log.error("",e);
        }
    }

    @SuppressWarnings("unchecked")
    private void delete(LsEntry target, String parentPath) {
        String path = parentPath + "/" + target.getFilename();
        try {
            // directory
            if (target.getAttrs().isDir()) {
//                Collection<ChannelSftp.LsEntry> files = sftp.ls(path);
                Collection<ChannelSftp.LsEntry> files = sftp.ls(path).stream().toList();
                for (LsEntry file : files) {
                    String name = file.getFilename();

                    if (".".equals(name) || "..".equals(name)) {
                        continue;
                    }

                    delete(file, path);
                }

                sftp.rmdir(path);
                log.info("delete directory: {}", path);
            }
            // file
            else {
                sftp.rm(path);
                log.info("delete file: {}", path);
            }
        } catch (Exception e) {
            log.error("",e);
        }
    }

}

