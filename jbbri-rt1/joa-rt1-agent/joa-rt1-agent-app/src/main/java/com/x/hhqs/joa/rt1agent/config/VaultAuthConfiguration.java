package com.x.hhqs.joa.rt1agent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "joa.rt1.vault-auth")
@RefreshScope
public class VaultAuthConfiguration {

	private boolean enabled = false;
	
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
