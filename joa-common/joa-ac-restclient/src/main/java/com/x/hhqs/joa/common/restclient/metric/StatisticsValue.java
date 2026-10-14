package com.x.hhqs.joa.common.restclient.metric;

public class StatisticsValue {
    public final String name;
    public final String unit;
    public final Object value;
    public final MetricType type;

    public StatisticsValue(String name, Object value, String unit, MetricType type) {
        this.name = name;
        this.unit = unit;
        this.value = value;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getUnit() {
        return unit;
    }

    public Object getValue() {
        return value;
    }

    public MetricType getType() {
        return type;
    }

    @Override
    public String toString() {
        return name + " = " + value + (unit != null ? (" " + unit) : "") + " (" + type.name() + ")";
    }
}
