package com.x.hhqs.joa.rmuid.model;

import java.io.Serializable;

public class HamObjectIdField implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;

    private String value;

    public HamObjectIdField(String name, Long value) {
        this.name = name;
        this.value = value == null ? null : value.toString();
    }

    public HamObjectIdField(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public HamObjectIdField(String name) {
        int index = name.indexOf("=");
        if (index >= 0) {
            this.name = name.substring(0, index);
            this.value = name.substring(index + 1);
        } else {
            this.name = name;
        }
    }

    public HamObjectIdField(HamObjectIdField copy) {
        this.name = copy.getName();
        this.value = copy.getStringValue();
    }

    public String getName() {
        return name;
    }

    public Long getLongValue() {
        return Long.parseLong(value);
    }

    public void setLongValue(Long value) {
        this.value = value == null ? null : value.toString();
    }

    public String getStringValue() {
        if (this.value != null)
            return this.value;

        return "";
    }

    public void setStringValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        if (this.value != null) {
            return "/" + this.name + "=" + this.value;
        } else {
            return "/" + this.name;
        }
    }

    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        final HamObjectIdField objectIdField = (HamObjectIdField) o;

        if (this.name != null ? !this.name.equals(objectIdField.name) : objectIdField.name != null)
            return false;
        if (this.value != null ? !this.value.equals(objectIdField.value) : objectIdField.value != null)
            return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = (name != null ? name.hashCode() : 0);
        result = 29 * result + (this.value != null ? this.value.hashCode() : 0);
        return result;
    }

    public boolean equalsType(HamObjectIdField objectIdField) {
        String[] names = name.split("[|]");
        for (String n : names) {
            if (n.equals(objectIdField.name)) {
                return true;
            }
        }

        return false;
    }

    public void setValue(Object value) {
        if (value == null) {
            this.value = null;
        } else {
            this.value = value.toString();
        }
    }

}
