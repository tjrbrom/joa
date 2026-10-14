package com.x.hhqs.joa.rmuid.model;


import java.io.Serializable;

public class InnerFriendlyNameField implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;

    private String value;

    public InnerFriendlyNameField(String name, Long value) {
        this.name = name;
        this.value = value == null ? null : value.toString();
    }

    public InnerFriendlyNameField(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public InnerFriendlyNameField(String nameField) {
        this.parse(nameField);
    }

    private void parse(String s) {
        if (s == null || s.trim().isEmpty()) {
            // should not happen
            this.name = "";
            return;
        }

        char[] c = s.toCharArray();

        int i = c.length;
        for (; i > 0; i--) {
            if (!Character.isDigit(c[i - 1])) {
                break;
            }
        }

        if (i == 0 || i == c.length) {
            this.name = s;
        } else {
            this.name = s.substring(0, i);
            this.value = s.substring(i);
        }
    }

    public InnerFriendlyNameField(InnerFriendlyNameField copy) {
        this.name = copy.getName();
        this.value = copy.getValue();
    }

    public String getName() {
        return name;
    }

    public Long getLongValue() {
        if (value == null) {
            return null;
        }
        return Long.parseLong(value);
    }

    public void setLongValue(Long value) {
        this.value = value == null ? null : value.toString();
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        if (this.value != null) {
            return this.name + this.value;
        } else {
            return this.name;
        }
    }

    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        final InnerFriendlyNameField objectIdField = (InnerFriendlyNameField) o;

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

    public boolean equalsType(InnerFriendlyNameField objectIdField) {
        return name.equals(objectIdField.name);
    }

}
