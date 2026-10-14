package com.x.hhqs.joa.rt1agent.security.dev;

import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@ConfigurationProperties(prefix = "joa.rt1.dev.user")
@RefreshScope
public class DevUserProperties {

    private String name = "admin";

    private Set<String> papGroups = Set.of("ALLPAPS");

    private Set<String> paps = Set.of("*");

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getPaps() {
        return paps;
    }

    public void setPaps(Set<String> paps) {
        this.paps = paps;
    }

    public Set<String> getPapGroups() {
        return papGroups;
    }

    public void setPapGroups(Set<String> papGroups) {
        this.papGroups = papGroups;
    }

}
