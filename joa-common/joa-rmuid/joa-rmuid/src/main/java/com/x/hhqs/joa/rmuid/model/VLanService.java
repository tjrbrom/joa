package com.x.hhqs.joa.rmuid.model;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class VLanService {
    private static final Map<String, Integer> types;

    static {
        types = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        types.put("single-tagged", 1);
        types.put("untagged", 2);
        types.put("priority-tagged", 3);
        types.put("transparent-forward", 4);
        types.put("S-VLAN-Tunnel-Priority-Tagged", 5);
        types.put("S-VLAN-Tunnel-Any-Frame", 6);
    }

    private String type;

    private Integer value;

    public VLanService(String type, Integer value) {
        this.type = type;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public int toCode() {
        Integer typeCode = types.get(type);
        if (typeCode == null) {
            throw new IllegalArgumentException("Unsupported service type: " + type);
        }

        if (value < 1 || value > 4096) {
            throw new IllegalArgumentException("vlan out of range (1-4096)");
        }

        return (typeCode << 12) | (value - 1);
    }

    public static String getType(int v) {
        for (Entry<String, Integer> entry : types.entrySet()) {
            if (entry.getValue() == v) {
                return entry.getKey();
            }
        }
        return null;
    }
}

