package com.x.hhqs.joa.rt1agent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "joa.application")
@RefreshScope
public class JbbriApplicationConfig {

	private CommandGroupConfig commandGroup;

	public CommandGroupConfig getCommandGroup() {
		return commandGroup;
	}

	public void setCommandGroup(CommandGroupConfig commandGroup) {
		this.commandGroup = commandGroup;
	}

}
