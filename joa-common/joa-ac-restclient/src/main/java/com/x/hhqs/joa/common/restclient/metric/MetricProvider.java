package com.x.hhqs.joa.common.restclient.metric;

public interface MetricProvider {
    String getName();

    String getUnit();

    MetricType getType();

    Object getValue();

    StatisticsValue getStatisticsValue();
}
