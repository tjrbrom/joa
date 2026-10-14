package com.x.hhqs.joa.alarm.contextfield.handlers;

public abstract class AbstractAttributeHandler implements AttributeHandler {
    protected static final String EMPTY = "";
    public abstract String getId();
    @Override
    public String toString() {
        return "alarmContext." + getId();
    }

    public String null2Empty(String val) {
        return val == null ? EMPTY : val;
    }
}