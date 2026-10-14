package com.x.hhqs.joa.alarm.file;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "joa-alarm.sftp")
@JsonIgnoreProperties(ignoreUnknown = true)
//@RefreshScope
public class DestinationNode {

    private NodeType type;

    private String path;

    private String ip;

//    private String osspath;

    private Integer port;

    private String user;

    private String password;

    private boolean fileDelete = false;

    private FileAutoDeleteStrategy fileAutoDeleteStrategy;

    private Map<String, String> properties;

    @Getter
    @Setter
    public static class FileAutoDeleteStrategy{
        private String keep;
    }

    public String getUploadPath() {
        String uploadPath = path;
/*        if (osspath != null && !osspath.isEmpty()) {
            uploadPath = osspath;
        }*/

        if (!uploadPath.startsWith("/")) {
            return "/" + uploadPath;
        }

        return uploadPath;
    }

    public NodeType getType() {
        return type;
    }

    public void setType(NodeType type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isFileDelete() {
        return fileDelete;
    }

    public void setFileDelete(boolean fileDelete) {
        this.fileDelete = fileDelete;
    }

    public FileAutoDeleteStrategy getFileAutoDeleteStrategy() {
        return fileAutoDeleteStrategy;
    }

    public void setFileAutoDeleteStrategy(FileAutoDeleteStrategy fileAutoDeleteStrategy) {
        this.fileAutoDeleteStrategy = fileAutoDeleteStrategy;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
}
