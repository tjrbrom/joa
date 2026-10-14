package com.x.hhqs.joa.alarm.file;

import java.io.Closeable;
import java.util.concurrent.TimeUnit;

public interface FileDeleter extends Closeable {

    String GLOBBING_ALL_FILES = "*";

    void deleteFilesByAliveTime(long aliveTime, TimeUnit unit) throws Exception;

    void deleteFilesByQuantityLimit(int maxQuantity) throws Exception;

}
