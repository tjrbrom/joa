package com.x.hhqs.joa.common.restclient;

public class AggregateContext extends Pair<String, Integer> {
    public AggregateContext(String contextName, Integer batchSize) {
        super(contextName, batchSize);
    }

    public String getContextName() {
        return getFirst();
    }

    public Integer getBatchSize() {
        return getSecond();
    }

    @Override
    public String toString() {
        return "AggregateContext{" +
                "m_contextname='" + getContextName() + '\'' +
                ", m_batchSize='" + getBatchSize() + '\'' +
                '}';
    }
}
