package com.x.hhqs.joa.tools.nacos;

import java.util.Map;

public class InitConfig {
    private String dataId;
    private String group;
    private String namespaceId;
    private String type;
    private String tag;
    private Map<String, String> data;

    // Getters and setters
    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public String toString() {
        return "[" + dataId + ", " + group + ", " + namespaceId + ", "
                + type + ", " + tag + "]";
    }
}
