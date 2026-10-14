package com.x.hhqs.joa.mds.sync;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "joa.mds.sync-task")
@RefreshScope
public class SyncTaskConfig {

    private boolean enabled = true;

    private long intervalMinutes = 60;

    private List<String> managerTypes = new ArrayList<>();
    
    private List<String> intentTypes = new ArrayList<>();

    public List<String> getManagerTypes() {
        return managerTypes;
    }

    public void setManagerTypes(List<String> managerTypes) {
        this.managerTypes = managerTypes;
    }

    public long getIntervalMinutes() {
        return intervalMinutes;
    }

    public void setIntervalMinutes(long intervalMinutes) {
        this.intervalMinutes = intervalMinutes;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getIntentTypes() {
        return intentTypes;
    }

    public void setIntentTypes(List<String> intentTypes) {
        this.intentTypes = intentTypes;
    }

}
