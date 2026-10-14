package com.x.hhqs.joa.common.sharedfileservice.utils;

import java.io.InputStream;

public class UploadFileInfo {
    private InputStream m_fileInputStream;
    private String m_fileName;
    private String m_targetFileDirectory;

    public UploadFileInfo(InputStream inputStream, String fileName, String directory) {
        m_fileInputStream = inputStream;
        m_fileName = fileName;
        m_targetFileDirectory = directory;
    }

    public InputStream getFileInputStream() {
        return m_fileInputStream;
    }

    public void setFileInputStream(InputStream fileInputStream) {
        m_fileInputStream = fileInputStream;
    }

    public String getFileName() {
        return m_fileName;
    }

    public void setFileName(String fileName) {
        m_fileName = fileName;
    }

    public String getTargetFileDirectory() {
        return m_targetFileDirectory;
    }

    public void setTargetFileDirectory(String directory) {
        m_targetFileDirectory = directory;
    }

}
