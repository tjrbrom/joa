package com.x.hhqs.joa.rmuid.model;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class InnerFriendlyName implements Serializable {

    private static final long serialVersionUID = 1L;

    private LinkedHashMap<String, InnerFriendlyNameField> friendlyNameFieldMap = new LinkedHashMap<>(10);
    
    private String friendlyName;

    public InnerFriendlyName(String friendlyName) {
        if (friendlyName == null) {
            friendlyName = "";
        }
        
        this.friendlyName = friendlyName;
        
        String[] pieces = friendlyName.split("[.]");
        for (String s : pieces) {
            if (!"".equals(s.trim())) {
                InnerFriendlyNameField field = new InnerFriendlyNameField(s);
                friendlyNameFieldMap.put(field.getName(), field);
            }
        }
    }

    public String getName(int i) {
        return (String) friendlyNameFieldMap.keySet().toArray()[i];
    }

    public Long getValue(int i) {
        return getIfnField(i).getLongValue();
    }

    public Long getValue(String fieldName) {
        InnerFriendlyNameField field = friendlyNameFieldMap.get(fieldName);
        if (field != null) {
            return field.getLongValue();
        }
        return null;
    }

    public InnerFriendlyNameField getIfnField(int i) {
        return friendlyNameFieldMap.get(friendlyNameFieldMap.keySet().toArray()[i]);
    }

    public List<InnerFriendlyNameField> getIfnFields() {
        return new ArrayList<InnerFriendlyNameField>(friendlyNameFieldMap.values());
    }

    public boolean isNameExist(String fieldName) {
        return friendlyNameFieldMap.containsKey(fieldName);
    }

    public int size() {
        return friendlyNameFieldMap.size();
    }

    public String getField(int i) {
        return getIfnField(i).toString();
    }

    @Override
    public String toString() {
        return getCompleteName();
    }

    public String getCompleteName() {
        return friendlyNameFieldMap.values().stream()
                .map(f -> f.toString())
                .collect(Collectors.joining("."));
    }

    public boolean isPrefixType(InnerFriendlyName other) {
        if (getIfnFields().size() > other.getIfnFields().size()) {
            return false;
        }
        for (int i = 0; i < getIfnFields().size(); i++) {
            InnerFriendlyNameField field = getIfnField(i);
            if (!field.equalsType(other.getIfnField(i))) {
                return false;
            }
        }
        return true;
    }

    public boolean equalsType(InnerFriendlyName other) {
        if (this.size() != other.size()) {
            return false;
        } else {
            Object[] values = friendlyNameFieldMap.values().toArray();
            Object[] othervalues = other.friendlyNameFieldMap.values().toArray();
            for (int i = 0; i < values.length; i++) {
                if (!((InnerFriendlyNameField) values[i]).equalsType((InnerFriendlyNameField) othervalues[i])) {
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

        final InnerFriendlyName other = (InnerFriendlyName) o;

        if (other.friendlyNameFieldMap.size() != this.friendlyNameFieldMap.size()) {
            return false;
        }

        // we know sizes are the same, so we can iterate over both
        Iterator<Entry<String, InnerFriendlyNameField>> thisIterator = friendlyNameFieldMap.entrySet().iterator();
        Iterator<Entry<String, InnerFriendlyNameField>> otherIterator = other.friendlyNameFieldMap.entrySet()
                .iterator();

        // compare the fields
        while (thisIterator.hasNext()) {
            Entry<String, InnerFriendlyNameField> thisField = thisIterator.next();
            Entry<String, InnerFriendlyNameField> otherField = otherIterator.next();

            if (!(thisField.getKey().equals(otherField.getKey())
                    && thisField.getValue().equals(otherField.getValue()))) {
                return false;
            }
        }

        return true;
    }

    public int hashCode() {
        return (friendlyNameFieldMap != null ? friendlyNameFieldMap.hashCode() : 0);
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

}
