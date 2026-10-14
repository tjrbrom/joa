package com.x.hhqs.joa.alarm.file;

import java.io.Closeable;


public interface FileUploader extends Closeable {

    void uploadFile(String remotePath, String sourceFilePath) throws Exception;

    void rename(String remotePath, String oldFileName, String newFileName);

    void delete(String remotePath, String fileName);

}
