package com.x.hhqs.joa.alarm.noncodedplugs.utils;

public class AlarmUtil {
    private static final String OPEN_BRACKET = "(";
    private static final String CLOSE_BRACKET = ")";
    private static final String QUESTION_MARK = "?";

    /**
     * Remove the revision from QName
     */
    public static String getAlarmIdentityWithOutRevision(String alarmIdentityQName) {
        if(alarmIdentityQName.contains("?revision=")) {
            String[] alarmTypeQualifier = alarmIdentityQName.split("\\?|\\)");//https://www.x.com/Fixed-Networks/BBA/yang/x-eqpt-alarm-types?revision=2021-10-28)disk-usage-exceeding
            return alarmTypeQualifier[0] + ")" + alarmTypeQualifier[2];
        }
        return alarmIdentityQName;
    }

    public static String getNamespace(String alarmTypeQualifier) {
        if (alarmTypeQualifier.contains(OPEN_BRACKET) && alarmTypeQualifier.contains(QUESTION_MARK)) {
            return alarmTypeQualifier.substring(alarmTypeQualifier.indexOf(OPEN_BRACKET) + 1, alarmTypeQualifier.indexOf(QUESTION_MARK));
        } else if(alarmTypeQualifier.contains(OPEN_BRACKET)){
            return alarmTypeQualifier.substring(alarmTypeQualifier.indexOf(OPEN_BRACKET) + 1, alarmTypeQualifier.indexOf(CLOSE_BRACKET));
        }
        return alarmTypeQualifier;
    }
}
