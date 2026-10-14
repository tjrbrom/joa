package com.x.hhqs.joa.alarm.service;

import com.x.hhqs.joa.alarm.file.DestinationNode;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public interface FileDeleteService {

    void deleteFilesByAliveTime(DestinationNode node, long aliveTime, TimeUnit timeUnit) throws IOException, Exception;

    void deleteFilesByQuantityLimit(DestinationNode node, int maxQuantity) throws Exception;
}
