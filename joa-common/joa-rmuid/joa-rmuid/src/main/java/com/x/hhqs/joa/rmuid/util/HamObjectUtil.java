package com.x.hhqs.joa.rmuid.util;

import com.x.hhqs.joa.rmuid.model.HamObjectId;
import com.x.hhqs.joa.rmuid.model.Pon;
import com.x.hhqs.joa.rmuid.model.Uni;
import com.x.hhqs.joa.rmuid.model.VLanService;

public class HamObjectUtil {
    static final HamObjectId SLOT_ID = new HamObjectId("/device|ip/slot");
    static final HamObjectId PON_ID = new HamObjectId("/device|ip/slot/pon");
    static final HamObjectId PON_VLAN_ID = new HamObjectId("/device|ip/vlan");
    static final HamObjectId ONT_ID = new HamObjectId("/device|ip/slot/pon/ont");
    static final HamObjectId ONTID_ID = new HamObjectId("/device|ip/slot/pon/ontid");
    static final HamObjectId POS_ID = new HamObjectId("/device|ip/slot/pon/splitter");
    static final HamObjectId UNI_ID = new HamObjectId("/device|ip/slot/pon/ont|ontid/uni");
    static final HamObjectId VLAN_PORT_ID = new HamObjectId("/device|ip/slot/pon/ont|ontid/uni/service");
    static final HamObjectId CAGE_ID = new HamObjectId("/device|ip/slot/cage");
    static final HamObjectId LLDP_ID = new HamObjectId("/device|ip/slot/eth/addrNr/timeMarkNr/remNr");

    // remove '-'
    // "/device=test01/slot=LT-4" -> "LT4"
    // "/device=test01/slot=NT-A" -> "NTA"
    /*-
     LT-1, LT-2, ...
     NT-A, NT-B
     NTIO, NTIO-A, NTIO-B
     ACU
     PSU-1, PSU-2
     FAN
     */
    public static String getSlot(HamObjectId hamObjectId) {
        if (!hamObjectId.hasRdn("slot")) {
            throw new IllegalArgumentException("Invalid slot: " + hamObjectId);
        }

        String slot = hamObjectId.getValue("slot");
        if (slot == null || slot.isBlank()) {
            throw new IllegalArgumentException("Invalid slot: " + hamObjectId);
        }

        return slot.replace("-", "");
    }

    // "/device=test01/slot=LT-4" -> 4
    public static int getLtNumber(HamObjectId hamObjectId) {
        String slot = hamObjectId.getValue("slot");
        if (slot == null || !slot.startsWith("LT-")) {
            throw new IllegalArgumentException("Invalid Board-slot: " + hamObjectId);
        }

        return Integer.parseInt(slot.substring(3));
    }

    /*-
     GPON       "/device=shelf-1/slot=LT-1/pon=PON-1-GPON"
     XGS        "/device=shelf-1/slot=LT-1/pon=PON-1-XGS"
     Dual GPON  "/device=shelf-1/slot=LT-1/pon=PON-1"           ?
     MPM        "/device=shelf-1/slot=LT-1/pon=PON-1"           ?
     Type B     "/device=shelf-1/slot=LT-1/pon=PON-1"           ?
     */
    public static int getPonNumber(HamObjectId hamObjectId) {
        String pon = hamObjectId.getValue("pon");
        if (pon == null || pon.isBlank()) {
            throw new IllegalArgumentException("Invalid Pon: " + hamObjectId);
        }

        return Integer.parseInt(pon.split("-")[1]);
    }

    public static Pon getPon(HamObjectId hamObjectId) {
        String value = hamObjectId.getValue("pon");
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Invalid Pon: " + hamObjectId);
        }

        Pon pon = getPon(value);
        if (pon.getId() < 1 || pon.getId() > 32) {
            throw new IllegalArgumentException("pon out of range (1-32)");
        }

        return pon;
    }

    // pon=PON-<pon-position>-<technology>
    public static Pon getPon(String pon) {
        String[] parts = pon.split("-", 3);
        String technology = "";
        if (parts.length == 3) {
            technology = parts[2];
        }

        return new Pon(Integer.valueOf(parts[1]), Pon.Technology.of(technology));
    }

    // splitter=<fiber-name>_id
    public static int getSplitterId(HamObjectId hamObjectId) {
        String splitterName = hamObjectId.getValue("splitter");
        if (splitterName == null || splitterName.isBlank()) {
            throw new IllegalArgumentException("Invalid splitter: " + hamObjectId);
        }

        int index = splitterName.lastIndexOf("_");
        if (index == -1) {
            throw new IllegalArgumentException("Invalid splitter: " + hamObjectId);
        }

        int id = Integer.parseInt(splitterName.substring(index + 1));
        if (id < 0 || id > 999) {
            throw new IllegalArgumentException("splitterId out of range (0-999)");
        }

        return id;
    }

    public static boolean isOnt(HamObjectId hamObjectId) {
        return ONT_ID.equalsType(hamObjectId);
    }

    public static boolean isOntId(HamObjectId hamObjectId) {
        return ONTID_ID.equalsType(hamObjectId);
    }

    public static boolean isPos(HamObjectId hamObjectId) {
        return POS_ID.equalsType(hamObjectId);
    }

    public static boolean isUni(HamObjectId hamObjectId) {
        return UNI_ID.equalsType(hamObjectId);
    }

    public static boolean isPon(HamObjectId hamObjectId) {
        return PON_ID.equalsType(hamObjectId);
    }

    public static boolean isVlanPort(HamObjectId hamObjectId) {
        return VLAN_PORT_ID.equalsType(hamObjectId);
    }

    public static boolean isCage(HamObjectId hamObjectId) {
        return CAGE_ID.equalsType(hamObjectId);
    }

    //
    public static boolean isPrefixOnt(HamObjectId hamObjectId) {
        return ONT_ID.isPrefixType(hamObjectId);
    }

    public static boolean isPrefixOntId(HamObjectId hamObjectId) {
        return ONTID_ID.isPrefixType(hamObjectId);
    }

    public static boolean isPrefixPos(HamObjectId hamObjectId) {
        return POS_ID.isPrefixType(hamObjectId);
    }

    public static boolean isPrefixUni(HamObjectId hamObjectId) {
        return UNI_ID.isPrefixType(hamObjectId);
    }

    public static boolean isPrefixPon(HamObjectId hamObjectId) {
        return PON_ID.isPrefixType(hamObjectId);
    }
    
    public static boolean isPrefixPonVlan(HamObjectId hamObjectId) {
        return PON_VLAN_ID.isPrefixType(hamObjectId);
    }

    public static boolean isPrefixVlanPort(HamObjectId hamObjectId) {
        return VLAN_PORT_ID.isPrefixType(hamObjectId);
    }

    public static boolean isPrefixCage(HamObjectId hamObjectId) {
        return CAGE_ID.isPrefixType(hamObjectId);
    }
    
    public static boolean isLldp(HamObjectId hamObjectId) {
        return LLDP_ID.isPrefixType(hamObjectId);
    }

    public static Uni parseUni(String s) {
        if (s == null || s.isBlank()) {
            throw new IllegalArgumentException("uni is empty");
        }

        char[] c = s.toCharArray();

        int i = c.length;
        for (; i > 0; i--) {
            if (!Character.isDigit(c[i - 1])) {
                break;
            }
        }

        Uni uni = new Uni();
        if (i == 0 || i == c.length) {
            uni.setType(s);
            uni.setPort(-1);
        } else {
            uni.setType(s.substring(0, i));
            uni.setPort(Integer.valueOf(s.substring(i)));
        }

        return uni;
    }

    public static VLanService parseVLanService(String s) {
        if (s == null || s.isBlank()) {
            throw new IllegalArgumentException("vlan service is empty");
        }

        int p = 3;
        int index = s.lastIndexOf("%3A");
        if (index == -1) {
            p = 1;
            index = s.lastIndexOf(":");
        }
        if (index == -1) {
            throw new IllegalArgumentException("invalid vlan service " + s);
        }
        String type = s.substring(0, index);
        Integer value = Integer.valueOf(s.substring(index + p));

        VLanService vlan = new VLanService(type, value);
        return vlan;
    }



}
