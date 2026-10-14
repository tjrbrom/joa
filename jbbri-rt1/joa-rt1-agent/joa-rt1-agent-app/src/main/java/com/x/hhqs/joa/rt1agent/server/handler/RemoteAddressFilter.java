package com.x.hhqs.joa.rt1agent.server.handler;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import com.x.hhqs.joa.rt1agent.config.ConnectionConfigurationProperties;
import org.apache.http.conn.util.InetAddressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;

import com.google.common.net.InetAddresses;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.ipfilter.AbstractRemoteAddressFilter;
import io.netty.handler.ipfilter.IpFilterRule;
import io.netty.handler.ipfilter.IpFilterRuleType;
import io.netty.handler.ipfilter.IpSubnetFilterRule;

@Component
@Sharable
public class RemoteAddressFilter extends AbstractRemoteAddressFilter<InetSocketAddress>
		implements ApplicationContextAware {
	private static final Logger LOGGER = LoggerFactory.getLogger(RemoteAddressFilter.class);

	private String configWhiteListkey = "joa.rt1.connection.ip-whitelist";

	private List<IpFilterRule> rules;

	private ApplicationContext context;

	@Autowired
	private ConnectionConfigurationProperties connectionProperties;

	@PostConstruct
	public void initFilterRules() {
		buildFilterRules(connectionProperties.getIpWhiteList());
	}

	private void buildFilterRules(List<String> whiteList) {
		List<IpFilterRule> list = new ArrayList<>();

		if (whiteList == null || whiteList.isEmpty()) {
			this.rules = list;
			return;
		}

		for (String ipAddress : whiteList) {
			// ipAddress/cidr or ipAddress (192.168.0.1/24 or 192.168.0.1)
			String[] array = ipAddress.split("[/]");

			if (array.length > 2) {
				LOGGER.warn("ipWhiteList contains invalid IpAddress: {}", ipAddress);
				continue;
			}

			String ip = array[0].trim();
			if (!validate(ip)) {
				LOGGER.warn("ipWhiteList contains invalid IpAddress: {}", ipAddress);
				continue;
			}

			int cidr = 32;
			if (array.length == 2) {
				try {
					cidr = Integer.valueOf(array[1].trim());
				} catch (Exception e) {
					LOGGER.warn("ipWhiteList contains invalid IpAddress: {}", ipAddress);
					continue;
				}
			}
			// 64 for ipv6
			else if (isIpv6(ip)) {
				cidr = 64;
			}

			list.add(new IpSubnetFilterRule(ip, cidr, IpFilterRuleType.ACCEPT));
		}

		this.rules = list;
	}

	private boolean isIpv6(String ip) {
		return InetAddressUtils.isIPv6Address(ip);
	}

	@Override
	protected boolean accept(ChannelHandlerContext ctx, InetSocketAddress remoteAddress) throws Exception {
		if (rules == null || rules.isEmpty()) {
			return true;
		}

		for (IpFilterRule rule : rules) {
			if (rule == null) {
				continue;
			}

			if (rule.matches(remoteAddress)) {
				return rule.ruleType() == IpFilterRuleType.ACCEPT;
			}
		}

		LOGGER.warn("Invalid client ipAddress: " + remoteAddress.toString());
		return false;
	}

	public static boolean validate(final String ip) {
		return InetAddresses.isInetAddress(ip);
	}

	@EventListener
	public void handleEnvironmentChangeEvent(EnvironmentChangeEvent event) {
		if (event.getKeys().contains(configWhiteListkey)) {
			ConfigurableEnvironment env = (ConfigurableEnvironment) context.getEnvironment();
			String value = env.getProperty(configWhiteListkey);
			
			LOGGER.info("{} changed, refresh filter rules. list: {}", configWhiteListkey, value);
			buildFilterRules(Arrays.asList(value.split(",")));
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.context = applicationContext;
	}

}
