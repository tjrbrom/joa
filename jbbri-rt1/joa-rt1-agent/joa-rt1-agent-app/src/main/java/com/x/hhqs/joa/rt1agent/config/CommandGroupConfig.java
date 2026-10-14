package com.x.hhqs.joa.rt1agent.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CommandGroupConfig {

	private boolean enabled = false;

	private Map<String, Set<String>> groups = new HashMap<>();

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Map<String, Set<String>> getGroups() {
		return groups;
	}

	public void setGroups(Map<String, Set<String>> groups) {
		this.groups = groups;
	}

}
