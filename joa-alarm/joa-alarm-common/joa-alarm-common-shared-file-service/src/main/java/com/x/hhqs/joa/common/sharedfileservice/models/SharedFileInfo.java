package com.x.hhqs.joa.common.sharedfileservice.models;

public class SharedFileInfo {

    private String m_fileName;
    private String m_parentDirectory;

    public SharedFileInfo(String fileName, String directory) {
        m_fileName = fileName;
        m_parentDirectory = directory;
    }

    public String getFileName() {
        return m_fileName;
    }

    public void setFileName(String fileName) {
        m_fileName = fileName;
    }

    public String getParentDirectory() {
        return m_parentDirectory;
    }

    public void setParentDirectory(String parentDirectory) {
        this.m_parentDirectory = parentDirectory;
    }

    @Override
    public String toString() {
        return "SharedFileInfo{" +
                "m_fileName='" + m_fileName + '\'' +
                ", m_parentDirectory='" + m_parentDirectory + '\'' +
                '}';
    }
}
