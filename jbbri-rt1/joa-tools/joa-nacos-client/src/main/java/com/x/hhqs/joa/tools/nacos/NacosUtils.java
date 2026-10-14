package com.x.hhqs.joa.tools.nacos;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class NacosUtils {

    public static final Pattern EXCLUDE_PROTOCOL_PATTERN = Pattern.compile("(?<=\\w{1,5}://)(.*)");
    public static final String IPV6_START_MARK = "[";
    public static final String IPV6_END_MARK = "]";
    public static final String IP_PORT_SPLITER = ":";

    public static Map<String, String> contentToMap(String content) {
        return Arrays.stream(content.split("\n"))
                .filter(line -> !line.isBlank())
                .map(line -> line.split("=", 2))
                .collect(Collectors.toMap(
                        parts -> parts[0],
                        parts -> parts.length > 1 ? parts[1] : "",
                        (existing, replacement) -> replacement,
                        LinkedHashMap::new));
    }

    public static String mapToContent(Map<String, String> map) {
        return map.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("\n"));
    }
    
    public static String resolveNacosUrl(String serverAddress) {
        String serverUrl = serverAddress;
        Matcher matcher = EXCLUDE_PROTOCOL_PATTERN.matcher(serverAddress);
        if (matcher.find()) {
            serverAddress = matcher.group(1);
        } else {
            serverUrl = "http://" + serverUrl;
        }
        String[] ipPortTuple = splitIPPortStr(serverAddress);
        if (ipPortTuple.length > 1) {
            return serverUrl;
        }
        return serverUrl + ":8848";
    }

    public static String[] splitIPPortStr(String str) {
        if (StringUtils.isBlank(str)) {
            throw new IllegalArgumentException("ip and port string cannot be empty!");
        }
        String[] serverAddrArr;
        if (str.startsWith(IPV6_START_MARK) && StringUtils.containsIgnoreCase(str, IPV6_END_MARK)) {
            if (str.endsWith(IPV6_END_MARK)) {
                serverAddrArr = new String[1];
                serverAddrArr[0] = str;
            } else {
                serverAddrArr = new String[2];
                serverAddrArr[0] = str.substring(0, (str.indexOf(IPV6_END_MARK) + 1));
                serverAddrArr[1] = str.substring((str.indexOf(IPV6_END_MARK) + 2));
            }
        } else {
            serverAddrArr = str.split(IP_PORT_SPLITER);
        }
        return serverAddrArr;
    }

}
