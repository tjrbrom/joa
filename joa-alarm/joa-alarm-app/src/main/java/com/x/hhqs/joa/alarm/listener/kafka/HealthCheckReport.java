package com.x.hhqs.joa.alarm.listener.kafka;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class HealthCheckReport {

    private final Map<String, Boolean> items = new LinkedHashMap<>();

    public HealthCheckReport add(String name, boolean result) {
        items.put(name, result);
        return this;
    }

    public boolean isHealthy() {
        return items.values().stream().allMatch(Boolean::booleanValue);
    }

    public Map<String, Boolean> getItems() {
        return Collections.unmodifiableMap(items);
    }

    @Override
    public String toString() {
        return items.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(", "));
    }
}
