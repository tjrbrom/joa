package com.x.hhqs.joa.rmuid.generator;

import java.util.Map;
import java.util.TreeMap;

public class RmUidGeneratorManager {

    private final Map<String, RmUidGenerator> defaultGenerators;

    private Map<String, RmUidGenerator> generators = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public RmUidGeneratorManager(Map<String, RmUidGenerator> defaultGenerators) {
        this.defaultGenerators = defaultGenerators;
    }

    public RmUidGenerator getRmUidGenerator(String resourceType) {
        RmUidGenerator generator = generators.get(resourceType);
        if (generator != null) {
            return generator;
        }
        return defaultGenerators.get(resourceType);
    }

    public void addGenerator(String resourceType, RmUidGenerator generator) {
        this.generators.put(resourceType, generator);
    }

}
