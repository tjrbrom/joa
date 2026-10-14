package com.x.hhqs.joa.in.util;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;

import org.springframework.util.StringUtils;

import com.x.hhqs.joa.common.rt1fwk.pojo.OssCmdMeta;

public class ConvertUtil {
	/**
	 * @param ponIdDesc LT6.PON1
	 * @return 1-1-6-1
	 */
	public static String ponIdDescToPonId(String ponIdDesc) {
		String[] split = ponIdDesc.split("\\.");
		return "1-1-" + split[0].replace("LT", "") + "-" + split[1].replace("PON", "");
	}

	/**
	 * <li>admin-status is down: other</li>
	 * <li>oper-status is up: UP</li>
	 * <li>oper-status is down and onu-last-down-reason is dgi: Power-Off</li>
	 * <li>oper-status is down and onu-last-down-reason is lobi: LOS</li>
	 */
	public static String getOperState(String adminState, String operStatus, String lastDownReason) {
		String operState = "";
		if ("DOWN".equalsIgnoreCase(adminState)) {
			operState = "other";
		} else {
			if ("up".equals(operStatus)) {
				operState = "UP";
			} else if ("down".equals(operStatus)) {
				// bbf-xpon-onu-types:dgi
				String downReason = lastDownReason.split(":")[1];
				if ("dgi".equals(downReason)) {
					operState = "Power-Off";
				} else if ("lobi".equals(downReason)) {
					operState = "LOS";
				} else {
					operState = "other";
				}
			}
		}
		return operState;
	}

	public static String onuPortToUniId(OssCmdMeta ossCmdMeta) {
		String onuPort = OssCmdMetaParser.onuPort(ossCmdMeta); // NA-NA-1-1
		if (onuPort == null) {
			// entire ont
			return "";
		}

		String onuCardNo = OssCmdMetaParser.onuCardNo(ossCmdMeta);
		String onuPortNo = OssCmdMetaParser.onuPortNo(ossCmdMeta);
		if ("14".equals(onuCardNo) && "1".equals(onuPortNo)) {
			return "VEIP";
		} else {
			return "LAN" + onuPortNo;
		}
	}

	public static String portIdToUniId(OssCmdMeta ossCmdMeta) {
		String portSlotNo = OssCmdMetaParser.portSlotNo(ossCmdMeta);
		String portPortNo = OssCmdMetaParser.portPortNo(ossCmdMeta);
		if ("14".equals(portSlotNo) && "1".equals(portPortNo)) {
			return "VEIP";
		} else {
			return "LAN" + portPortNo;
		}
	}

	public static String uniIdToOnuPort(String uniId) {
		String onuPort = "";
		if (uniId.startsWith("LAN")) {
			onuPort = "NA-NA-1-" + uniId.replaceAll("LAN", "");
		} else if ("VEIP".equals(uniId)) {
			onuPort = "NA-NA-14-1";
		}
		return onuPort;
	}

	public static String encodeURI(String input) throws UnsupportedEncodingException {
		return URLEncoder.encode(input.replace(".", "__d_"), "UTF-8");
	}

	public static String kbitToMbit(String bit) {
		return divideByThousand(bit);
	}

	public static String mToKm(String m) {
		return divideByThousand(m);
	}

	public static String bitToMbit(String bit) {
		return divideByMillion(bit);
	}

	public static String bitToKbit(String bit) {
		return divideByThousand(bit);
	}

	public static String hexToAscii(String hexStr) {
		if (!StringUtils.hasText(hexStr)) {
			return "";
		}

		StringBuilder output = new StringBuilder("");
		for (int i = 0; i < hexStr.length(); i += 2) {
			String str = hexStr.substring(i, i + 2);
			output.append((char) Integer.parseInt(str, 16));
		}
		return output.toString();
	}

	// input / 1000
	private static String divideByThousand(String str) {
		return divideByGiven(str, "1000");
	}

	// input / 1000000
	private static String divideByMillion(String str) {
		return divideByGiven(str, "1000000");
	}

	public static String divideByGiven(String input, String given) {
		if (!StringUtils.hasText(input)) {
			return "";
		}

		BigDecimal divisor = new BigDecimal(given);
		return new BigDecimal(input).divide(divisor).toString();
	}
}
