package com.x.hhqs.joa.alarm.file;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.TimeUnit;

@Setter
@Getter
public class FileDeleteStrategy {

    public enum Type {
        ALIVE_TIME, QUANTITY_LIMIT
    }

    private Type type;

    // set when type = Type.ALIVE_TIME
    private long aliveTime = -1L;

    private TimeUnit timeUnit = TimeUnit.DAYS;

    // set when type = Type.QUANTITY_LIMIT
    private int maxQuantity = -1;

    private FileDeleteStrategy() {
        //
    }

    public static FileDeleteStrategy quantityLimitStrategy(int maxQuantity) {
        FileDeleteStrategy strategy = new FileDeleteStrategy();
        strategy.setType(Type.QUANTITY_LIMIT);
        strategy.setMaxQuantity(maxQuantity);

        return strategy;
    }

    public static FileDeleteStrategy aliveTimeStrategy(int aliveTime, TimeUnit timeUnit) {
        FileDeleteStrategy strategy = new FileDeleteStrategy();
        strategy.setType(Type.ALIVE_TIME);
        strategy.setAliveTime(aliveTime);
        strategy.setTimeUnit(timeUnit);

        return strategy;
    }

    @Override
    public String toString() {
        return "FileDeleteStrategy [type=" + type + ", aliveTime=" + aliveTime + ", timeUnit=" + timeUnit
                + ", maxQuantity=" + maxQuantity + "]";
    }
}
