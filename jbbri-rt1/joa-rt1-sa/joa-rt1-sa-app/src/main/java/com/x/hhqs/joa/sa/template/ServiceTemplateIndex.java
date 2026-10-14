package com.x.hhqs.joa.sa.template;

import java.util.Objects;

public class ServiceTemplateIndex {
    private String oltType;
    private String ponType;
    private String onuType;
    private String commandCode;

    public ServiceTemplateIndex() {
    }

    public ServiceTemplateIndex(String oltType, String ponType, String onuType, String commandCode) {
        this.oltType = oltType;
        this.ponType = ponType;
        this.onuType = onuType;
        this.commandCode = commandCode;
    }

    public String getOltType() {
        return oltType;
    }

    public void setOltType(String oltType) {
        this.oltType = oltType;
    }

    public String getPonType() {
        return ponType;
    }

    public void setPonType(String ponType) {
        this.ponType = ponType;
    }

    public String getOnuType() {
        return onuType;
    }

    public void setOnuType(String onuType) {
        this.onuType = onuType;
    }

    public String getCommandCode() {
        return commandCode;
    }

    public void setCommandCode(String commandCode) {
        this.commandCode = commandCode;
    }

    @Override
    public String toString() {
        return "Index[" +
                "oltType='" + oltType + '\'' +
                ", ponType='" + ponType + '\'' +
                ", onuType='" + onuType + '\'' +
                ", commandCode='" + commandCode + '\'' +
                ']';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceTemplateIndex that = (ServiceTemplateIndex) o;
        return Objects.equals(oltType, that.oltType) && Objects.equals(ponType, that.ponType) && Objects.equals(onuType, that.onuType) && Objects.equals(commandCode, that.commandCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(oltType, ponType, onuType, commandCode);
    }
}
