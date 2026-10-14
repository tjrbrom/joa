package com.x.hhqs.joa.alarm.utils;

import com.x.hhqs.joa.rmuid.generator.RmType;

public class HamUtils {

    private static final HamUtils INSTANCE = new HamUtils();

    private static HamUtils clientUtils;

    public static void setInstanceForTest(HamUtils instance) {
        clientUtils = instance;
    }

    private HamUtils() {}

    public static HamUtils getInstance() {
        if (clientUtils != null) {
            return clientUtils;
        }
        return INSTANCE;
    }

    public String parseHamTypeWithHamFn(String hamObjectFriendlyName) {
        if (hamObjectFriendlyName == null || hamObjectFriendlyName.isBlank()) {
            return "";
        }

        String hamType = hamObjectFriendlyName.substring(0, hamObjectFriendlyName.indexOf(":"));

        return hamType;
    }

    public String hamType2RmuidType(String hamObjectFriendlyName) {
        String hamType = parseHamTypeWithHamFn(hamObjectFriendlyName);

        return switch (hamType) {
            // OMC
            case "NA" -> RmType.OMC.getValue();
            // OLT
            case "Device" -> RmType.OLT.getValue();
            // CRD
            case "Slot" -> RmType.Board.getValue();
            // PRT
            case "Cage" -> RmType.Port.getValue();
            // PRT
            case "PON Port" -> RmType.Port.getValue();
            // PRT
            case "PON Link" -> RmType.Port.getValue();
            // ONU
            case "ONT" -> RmType.ONU.getValue();
            // PRT
            case "UNI" -> RmType.Port.getValue();
            // PTV
            case "Service" -> RmType.PtpToVLAN.getValue();
            // VLN
            case "Network VLAN Interface" -> RmType.PonVLAN.getValue();
            // PRT
            case "Ethernet Port" -> RmType.Port.getValue();
            // LAG
            case "LAG" -> RmType.LAG.getValue();
            // OLT
            default -> RmType.OLT.getValue();
        };
    }


}
