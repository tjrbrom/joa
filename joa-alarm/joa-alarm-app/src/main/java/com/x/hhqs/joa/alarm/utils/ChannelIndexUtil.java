package com.x.hhqs.joa.alarm.utils;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ChannelIndexUtil {

	private ChannelIndexUtil() {
		// empty
	}

	public static int getChannelIndex(String ipAddress, int channelNumber) {
		try {
			return getChannelIndex(InetAddress.getByName(ipAddress), channelNumber);
		} catch (UnknownHostException e) {
			// NOSONAR: fallback to 0 is acceptable if IP resolution fails, handled upstream
			return 0;
		}
	}

	public static int getChannelIndex(InetAddress inetAddress, int channelNumber) {
		if (channelNumber == 1) {
			return 0;
		}

		byte[] bytes = inetAddress.getAddress();
		BigInteger modValue = new BigInteger(1, bytes).mod(BigInteger.valueOf(channelNumber));

		return modValue.intValue();
	}

	public static BigInteger ipAddressToBigInteger(InetAddress inetAddress) {
		return new BigInteger(1, inetAddress.getAddress());
	}

}