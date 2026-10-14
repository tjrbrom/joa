package com.x.hhqs.joa.alarm.utils;

import java.util.Map;
import java.util.TreeMap;

public class AlarmFieldManagerUtil {
    // ignore key case
    public static <V> Map<String, V> newCaseInsensitiveMap() {
        return new TreeMap<String, V>(String.CASE_INSENSITIVE_ORDER);
    }
}
