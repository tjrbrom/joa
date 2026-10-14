package com.x.hhqs.joa.alarm.utils;

public class AlarmFieldUtil {
    /**
     * Escapes the special characters ':', ';', and '\' in the input string.
     * ':'  -> '\\:'
     * ';'  -> '\\;'
     * '\'  -> '\\\\'
     *
     * @param input The original string to be escaped
     * @return The escaped string
     */
    public static String escapeSpecialCharacters(String input) {
        if (input == null) {
            return null;
        }
        // First escape backslash to avoid double escaping
        String escaped = input.replace("\\", "\\\\");
        escaped = escaped.replace(":", "\\:");
        escaped = escaped.replace(";", "\\;");
        return escaped;
    }
}
