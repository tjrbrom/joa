package com.x.hhqs.joa.common.restclient;

public interface HttpMonitor {
    void updateRequestCount(int delta, String baseUrl, String target, boolean success);
    void updateRequestTime(long delta, String baseUrl, String target);
}
