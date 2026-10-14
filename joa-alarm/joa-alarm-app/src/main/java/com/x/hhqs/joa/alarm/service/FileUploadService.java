package com.x.hhqs.joa.alarm.service;

import java.util.Collection;

public interface FileUploadService {
    /**
     * @param remotePath
     *            the completed path where file will be upload to contains two part:
     *            1. root path
     *
     *            root path is defined in omcfile.xml;
     *            for example, <path>/export/alarm</path>
     *            root path is '/export/alarm'
     *
     *            2. remotePath.
     *            remotePath is the path in root path;
     *            for example, root path is '/export/alarm', remotePath is '/20170224'
     *            file will be upload to '/export/alarm/20170224'
     *
     *            Note: all directory of path will be auto create if not exists.
     * @param sourceFilePath
     *            path of file to upload
     * @throws Exception
     */
    void uploadFile(String remotePath, String sourceFilePath) throws Exception;
    /**
     * @param rootPath + remotePath
     * @return
     */
    String getFullRemotePath(String remotePath);

    void rename(String remotePath, String oldFileName, String newFileName) throws Exception;

    void delete(String remotePath, String fileName) throws Exception;

}
