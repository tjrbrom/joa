package com.x.hhqs.joa.alarm.utils;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@NoArgsConstructor
public class SyncAlarmFile {

    private static final String DATE_PATTERN = "yyyyMMddHHmmss-";

    private static final String dataType = "FM-";

    private static final String meType = "OMC-";

    private static final String extension = ".txt";

    private static final String tmp_extension = ".tmp";

    @Setter
    @Getter
    private int index = 0;

    @Getter
    private String reqSyncId;

    private String prefixName;

    private String version;

    public SyncAlarmFile(String reqSyncId) {
        super();
        this.reqSyncId = reqSyncId;
        this.prefixName = getPrefix() + getNewDateTime() + reqSyncId;
    }

    public SyncAlarmFile(String reqSyncId,String version) {
        super();
        this.reqSyncId = reqSyncId;
        this.version = version;
        this.prefixName = getPrefix() + getNewDateTime() + reqSyncId;
    }

    private String getPrefix() {
//		String serverNumber = OasPropertiesHelper.getProperty("omc.hostno");
        String serverNumber = "01";
//		if (serverNumber == null || serverNumber.trim().isEmpty()) {
//			serverNumber = System.getProperty("omc.hostno", "01");
//		}
        serverNumber = serverNumber + "-";

        String dataVersion = version + "-";

        return dataType + meType + serverNumber + dataVersion;
    }

    private SyncAlarmFile(String reqSyncId, String prefixName, int index) {
        super();
        this.reqSyncId = reqSyncId;
        this.prefixName = prefixName;
        this.index = index;
    }

    public String getName() {
        if (index == 0) {
            return prefixName + extension;
        }

        return getNameWithIndex();
    }

    public String getTmpName() {
        return getName() + tmp_extension;
    }

    public SyncAlarmFile getNext() {
        return new SyncAlarmFile(reqSyncId, prefixName, index + 1);
    }

    private String getNameWithIndex() {
        return prefixName + getNameIndex() + extension;
    }

    public String getNameIndex() {
        return String.format("-%03d", index);
    }

    private static String getNewDateTime() {
        return getDateTime(new Date());
    }

    private static String getDateTime(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
        return sdf.format(date);
    }

}
