package com.x.hhqs.joa.alarm.logger;

import com.x.hhqs.joa.alarm.vo.OmcAlarmVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;

public class AlarmLogger {
    private final static Logger LOGGER = LoggerFactory.getLogger(AlarmLogger.class);

    private static String logName = "oasAlarm.log";

    public static String ALARM = "ALARM";
    public static String USER = "USER";
    public static String LOGIN = "LOGIN";
    public static String LOGOUT = "LOGOUT";
    public static String SEND = "SEND";
    public static String SyncAlarmMsg = "SyncAlarmMsg";

    public static void log(String msg) {
        LOGGER.debug(msg);
    }

    public static void receive(OmcAlarmVO alarm, String user, int port, SocketAddress socketAddress) {
        LOGGER.info("{},{},{},{},{},{}", ALARM, SEND, user, socketAddress, port, alarm.getJsonValue());
    }

    public static void send(OmcAlarmVO alarm, String user, int port, SocketAddress socketAddress) {
        log(String.format("%s,%s,%s,%s,%s,%s", ALARM, SEND, user, socketAddress, port, alarm.getAlarmSeq()));
        receive(alarm, user, port, socketAddress);
    }

    public static void userLogin(String user, int port, SocketAddress socketAddress) {
        log(String.format("%s,%s,%s,%s,%s", USER, LOGIN, user, socketAddress.toString(), port));
    }
    public static void userLogout(String user, int port, SocketAddress socketAddress) {
        log(String.format("%s,%s,%s,%s,%s",  USER, LOGOUT, user, socketAddress.toString(), port));
    }

    public static void userSyncAlarmMsg(String user, SocketAddress socketAddress, String reqId, Long alarmSeq) {
        log(String.format("%s,%s,%s,%s,reqId=%s,alarmSeq=%s", USER, SyncAlarmMsg, user, socketAddress, reqId, alarmSeq));
    }

}

