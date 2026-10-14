package com.x.hhqs.joa.common.sharedfileservice.utils;

public class FileServerInfo {
    
    public static final String SLASH = "/";
    public static final String SHAREDFS = "sharedfs";

    public static final String DEFAULT_ROOT_DIR = "/webdav";
    public static final String DASH = "-";

    private final String m_username;
    private final String m_password;
    private final String m_url;
    private final String m_basedirectory;
    private final String m_rootPath;
    private final String m_basePath;
    private final String m_coreInstanceName;

    public FileServerInfo(String username, String password, String url, String baseDirectory, String coreInstanceName) {
        m_coreInstanceName = coreInstanceName;
        m_username = username;
        m_password = password;
        m_url = url;
        m_basedirectory = baseDirectory;
        m_rootPath = buildRootPath(false);
        m_basePath = buildFileServerBasePath(false);
    }

    public String getUsername() {
        return m_username;
    }

    public String getPassword() {
        return m_password;
    }

    public String getUrl() {
        return m_url;
    }

    public String getBaseDirectory() {
        return m_basedirectory;
    }

    public String getRootPath() {
        return m_rootPath;
    }
    
    public String getBasePath() {
        return m_basePath;
    }
    
    private String buildRootPath(boolean withCredential) {
        StringBuilder rootPathBuilder = new StringBuilder();
        if (withCredential) {
            String[] rootPathsSplit  = m_url.split("://");
            rootPathBuilder.append(rootPathsSplit[0]);
            rootPathBuilder.append("://");
            rootPathBuilder.append(m_username);
            rootPathBuilder.append(":");
            rootPathBuilder.append(m_password);
            rootPathBuilder.append("@");
            rootPathBuilder.append(rootPathsSplit[1]);
        } else {
            rootPathBuilder.append(m_url);
        }
        rootPathBuilder.append(DEFAULT_ROOT_DIR);
        return rootPathBuilder.toString();
    }
    
    public String buildFileServerBasePath(boolean withCredential) {
        StringBuilder basePathBuilder = new StringBuilder();
        basePathBuilder.append(buildRootPath(withCredential)).append(SLASH);
        basePathBuilder.append(m_coreInstanceName).append(SLASH);
        basePathBuilder.append(SHAREDFS).append(SLASH);
        return basePathBuilder.toString();
    }
    
    public String buildBaseDirectory() {
        StringBuilder baseDirectory = new StringBuilder();
        baseDirectory.append(DEFAULT_ROOT_DIR.replace("/", "")).append(SLASH);
        baseDirectory.append(m_coreInstanceName).append(SLASH);
        baseDirectory.append(SHAREDFS);
        return baseDirectory.toString();
    }
}