package com.x.hhqs.joa.common.restclient.metric;

import java.util.Collection;

public interface StatisticsConsumer {
    default boolean autoRegister() {
        return true;
    }
    void consumeStatistics(Collection<StatisticsValue> statisticsValues);
}
