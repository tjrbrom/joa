package com.x.hhqs.joa.common.restclient.ha;

public interface HighAvailabilityStateChangeListener {
    void onStateChange(HighAvailabilityState HAState);
}
