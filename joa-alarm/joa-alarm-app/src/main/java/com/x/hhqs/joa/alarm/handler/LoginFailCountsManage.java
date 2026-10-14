package com.x.hhqs.joa.alarm.handler;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class LoginFailCountsManage {

    private static final ConcurrentHashMap<Channel, AtomicInteger> loginFailCounts = new ConcurrentHashMap<>();

    /**
     * close channel if login fail 3 times
     * @param ch channel
     */
    public static void checkLoginFailTimes(Channel ch) {
        log.warn("check loginFailCounts channel channel id {}", ch.id());
        AtomicInteger times = loginFailCounts.computeIfAbsent(ch, k -> new AtomicInteger(0));
        if (times.incrementAndGet() >= 3) {
            log.warn("Client login fail more than 3 times, close connection. {}", ch.remoteAddress().toString());
            ch.close();
            removeLoginFailCounts(ch);
        } else {
            loginFailCounts.put(ch, times);
        }
    }

    public static void removeLoginFailCounts(Channel ch) {
        loginFailCounts.remove(ch);
        log.debug("login fail counts size {}", loginFailCounts.size());
    }

}
