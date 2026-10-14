package com.x.hhqs.joa.rmuid.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import com.x.hhqs.joa.rmuid.exception.JoaRmuidException;

public class HamObjectId implements Serializable {

    private static final long serialVersionUID = 1L;

    private LinkedHashMap<String, HamObjectIdField> objectIdFieldMap = new LinkedHashMap<>(10);
    
    private String originName;
    
    public HamObjectId(String name) {
        this.originName = name;
        
        if (name == null) {
            name = "";
        }

        String[] pieces = name.split("/");
        for (String s : pieces) {
            if (!"".equals(s.trim())) {
                HamObjectIdField field = new HamObjectIdField(s);
                objectIdFieldMap.put(field.getName(), field);
            }
        }
    }

    public String getName(int i) {
        return (String) objectIdFieldMap.keySet().toArray()[i];
    }

    public String getValue(String rdnName) {
        HamObjectIdField field = objectIdFieldMap.get(rdnName);
        if (field != null) {
            return field.getValue();
        }
        return null;
    }

    public boolean hasRdn(String rdnName) {
        return objectIdFieldMap.get(rdnName) != null;
    }

    public String getValue(int i) {
        return getRdn(i).getValue();
    }

    public Long getLongValue(int i) {
        return getRdn(i).getLongValue();
    }

    public HamObjectIdField getRdn(int i) {
        return objectIdFieldMap.get(objectIdFieldMap.keySet().toArray()[i]);
    }

    public HamObjectIdField getRdn(String name) {
        return objectIdFieldMap.get(name);
    }

    public HamObjectIdField lastRdn() {
        Object[] keys = objectIdFieldMap.keySet().toArray();
        return objectIdFieldMap.get(keys[keys.length - 1]);
    }

    public int size() {
        return objectIdFieldMap.size();
    }

    @Override
    public String toString() {
        return getCompleteName();
    }

    public String getCompleteName() {
        StringBuilder sb = new StringBuilder();
        for (HamObjectIdField objectIdField : objectIdFieldMap.values()) {
            sb.append(objectIdField);
        }
        return sb.toString();
    }

    public boolean isPrefixType(HamObjectId objectId) {
        if (getRdns().size() > objectId.getRdns().size()) {
            return false;
        }
        for (int i = 0; i < getRdns().size(); i++) {
            HamObjectIdField objectIdField = getRdn(i);
            if (!objectIdField.equalsType(objectId.getRdn(i))) {
                return false;
            }
        }
        return true;
    }

    public List<HamObjectIdField> getRdns() {
        return new ArrayList<HamObjectIdField>(objectIdFieldMap.values());
    }

    public boolean equalsType(HamObjectId other) {
        if (this.size() != other.size()) {
            return false;
        } else {
            Object[] values = objectIdFieldMap.values().toArray();
            Object[] othervalues = other.objectIdFieldMap.values().toArray();
            for (int i = 0; i < values.length; i++) {
                if (!((HamObjectIdField) values[i]).equalsType((HamObjectIdField) othervalues[i])) {
                    return false;
                }
            }
            return true;
        }
    }

    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        final HamObjectId objectId = (HamObjectId) o;

        if (objectId.objectIdFieldMap.size() != this.objectIdFieldMap.size()) {
            return false;
        }

        // we know sizes are the same, so we can iterate over both
        Iterator<Entry<String, HamObjectIdField>> thisIterator = objectIdFieldMap.entrySet().iterator();
        Iterator<Entry<String, HamObjectIdField>> otherIterator = objectId.objectIdFieldMap.entrySet().iterator();

        // compare the fields
        while (thisIterator.hasNext()) {
            Entry<String, HamObjectIdField> thisField = thisIterator.next();
            Entry<String, HamObjectIdField> otherField = otherIterator.next();

            if (!(thisField.getKey().equals(otherField.getKey())
                && thisField.getValue().equals(otherField.getValue()))) {
                return false;
            }
        }

        return true;
    }

    public int hashCode() {
        return (objectIdFieldMap != null ? objectIdFieldMap.hashCode() : 0);
    }

    public void add(HamObjectIdField objectIdField) {
        if (objectIdField == null) {
            throw new JoaRmuidException("Cannot add a null field to an HamObjectId");
        }
        objectIdFieldMap.put(objectIdField.getName(), objectIdField);
    }

    public void add(HamObjectId partialObjectId) {
        for (HamObjectIdField field : partialObjectId.getRdns()) {
            if (field != null) {
                objectIdFieldMap.put(field.getName(), field);
            }
        }
    }

    public String getOriginName() {
        return originName;
    }
}
