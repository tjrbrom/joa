package com.x.hhqs.joa.alarm.file.utils;


import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.util.Collection;

public final class SftpFiles {

    private SftpFiles() {
        //
    }

    @SuppressWarnings("unchecked")
    public static void walkFileTree(ChannelSftp sftp, String start,
                                    RemoteFileVisitor<? super LsEntry> visitor) throws Exception {

//        Collection<ChannelSftp.LsEntry> files = sftp.ls(start);
        Collection<ChannelSftp.LsEntry> files = sftp.ls(start).stream().toList();

        for (LsEntry file : files) {
            String name = file.getFilename();

            // can it be './' or '../'?
            if (".".equals(name) || "..".equals(name)) {
                continue;
            }

            FileVisitResult result = walkFileTree(sftp, start, file, visitor);

            // returning null will cause NPE to be thrown
            if (result == null || result == FileVisitResult.TERMINATE)
                break;

            // skip remaining siblings in this directory
            if (result == FileVisitResult.SKIP_SIBLINGS)
                break;
        }
    }

    @SuppressWarnings("unchecked")
    private static FileVisitResult walkFileTree(ChannelSftp sftp, String parentPath, LsEntry lsEntry,
                                                RemoteFileVisitor<? super LsEntry> visitor) throws Exception {

        // visit file
        if (!lsEntry.getAttrs().isDir()) {
            return visitor.visitFile(lsEntry, parentPath);
        }

        IOException ioe = null;
        // visit directory
        FileVisitResult result = visitor.preVisitDirectory(lsEntry, parentPath);
        if (result != FileVisitResult.CONTINUE) {
            return result;
        }

        if (result == FileVisitResult.SKIP_SUBTREE) {
            return FileVisitResult.CONTINUE;
        }

        String path = parentPath + "/" + lsEntry.getFilename();
//        Collection<ChannelSftp.LsEntry> files = sftp.ls(path);
        Collection<ChannelSftp.LsEntry> files = sftp.ls(path).stream().toList();

        for (LsEntry file : files) {
            String name = file.getFilename();

            // can it be './' or '../'?
            if (".".equals(name) || "..".equals(name)) {
                continue;
            }

            result = walkFileTree(sftp, path, file, visitor);

            // returning null will cause NPE to be thrown
            if (result == null || result == FileVisitResult.TERMINATE)
                return result;

            // skip remaining siblings in this directory
            if (result == FileVisitResult.SKIP_SIBLINGS)
                break;
        }

        return visitor.postVisitDirectory(lsEntry, parentPath, ioe);
    }
}
