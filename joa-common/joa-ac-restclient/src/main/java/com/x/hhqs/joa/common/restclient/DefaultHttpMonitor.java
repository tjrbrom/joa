package com.x.hhqs.joa.common.restclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

public class DefaultHttpMonitor implements HttpMonitor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultHttpMonitor.class);
    private static final AtomicLong COUNT = new AtomicLong(1);
    @Override
    public void updateRequestCount(int delta, String baseUrl, String target, boolean success) {
        LOGGER.debug("HttpMonitor-RequestCount: {}, {}", COUNT.getAndAdd(delta), success ? "succeed" : "failed");
    }

    @Override
    public void updateRequestTime(long delta, String baseUrl, String target) {
        LOGGER.debug("HttpMonitor-RequestTime: {}ms", delta);
    }
}
