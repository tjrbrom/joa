package com.x.hhqs.joa.alarm.service.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class ChannelAlarmSeqManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelAlarmSeqManager.class);

    private final Map<Integer, AtomicLong> seqMap = new ConcurrentHashMap<>();
    private final Map<Integer, ReentrantLock> lockMap = new ConcurrentHashMap<>();
    private final AtomicBoolean isReady = new AtomicBoolean(false);

    // Thread-local to track alarmSeq allocations per thread per channel
    private final ThreadLocal<Map<Integer, List<Long>>> threadLocalAllocated = ThreadLocal.withInitial(HashMap::new);

    public void initialize(Map<Integer, Long> initialSeqMap) {
        for (Map.Entry<Integer, Long> entry : initialSeqMap.entrySet()) {
            int channel = entry.getKey();
            long seq = entry.getValue();
            lockMap.putIfAbsent(channel, new ReentrantLock());
            seqMap.putIfAbsent(channel, new AtomicLong(seq));
        }
        if (!initialSeqMap.isEmpty()) {
            isReady.set(true);
            LOGGER.info("ChannelAlarmSeqManager initialized with {} channels: {}",
                    initialSeqMap.size(), initialSeqMap.keySet());
        } else {
            LOGGER.warn("ChannelAlarmSeqManager initialization skipped: no channel sequence data found.");
        }
    }

    public boolean isReady() {
        return isReady.get();
    }

    public void begin(int channelIndex) {
        ReentrantLock lock = lockMap.get(channelIndex);
        if (lock == null) throw new IllegalStateException("Channel not initialized: " + channelIndex);
        lock.lock(); // Acquire lock for current thread
        threadLocalAllocated.get().remove(channelIndex); // Clear any prior state for this thread-channel
    }

    public long next(int channelIndex) {
        AtomicLong seq = seqMap.get(channelIndex);
        if (seq == null) throw new IllegalStateException("Channel not initialized: " + channelIndex);

        long current = seq.get();
        long next = (current >= Integer.MAX_VALUE) ? 1L : current + 1;
        seq.set(next);

        threadLocalAllocated.get()
                .computeIfAbsent(channelIndex, k -> new ArrayList<>())
                .add(next);

        return next;
    }

    public void commit(int channelIndex) {
        threadLocalAllocated.get().remove(channelIndex); // Drop record of allocated values
        unlock(channelIndex);
    }

    public void rollback(int channelIndex) {
        List<Long> allocated = threadLocalAllocated.get().get(channelIndex);
        if (allocated != null && !allocated.isEmpty()) {
            long minAllocated = Collections.min(allocated);
            AtomicLong seq = seqMap.get(channelIndex);
            // Rollback to one before the lowest allocated sequence
            seq.set(minAllocated - 1);
        }
        threadLocalAllocated.get().remove(channelIndex);
        unlock(channelIndex);
    }

    private void unlock(int channelIndex) {
        ReentrantLock lock = lockMap.get(channelIndex);
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
