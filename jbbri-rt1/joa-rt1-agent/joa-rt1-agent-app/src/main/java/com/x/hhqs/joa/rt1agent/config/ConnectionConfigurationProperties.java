package com.x.hhqs.joa.rt1agent.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "joa.rt1.connection")
@RefreshScope
public class ConnectionConfigurationProperties {
	
	private int port = 12015;
	
	private String encoding = "UTF-8";
	
	private int maxConnections = 5;
	
	private int idleTimeoutMinutes = 30;

	private List<String> ipWhiteList = new ArrayList<String>();

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
	
	public int getIdleTimeoutMinutes() {
		return idleTimeoutMinutes;
	}

	public void setIdleTimeoutMinutes(int idleTimeoutMinutes) {
		this.idleTimeoutMinutes = idleTimeoutMinutes;
	}

	public List<String> getIpWhiteList() {
		return ipWhiteList;
	}

	public void setIpWhiteList(List<String> ipWhiteList) {
		this.ipWhiteList = ipWhiteList;
	}

	public int getMaxConnections() {
		return maxConnections;
	}

	public void setMaxConnections(int maxConnections) {
		this.maxConnections = maxConnections;
	}
	
}
