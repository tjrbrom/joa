package com.x.hhqs.joa.in.util;

import org.springframework.util.StringUtils;

import com.x.hhqs.joa.common.rt1fwk.pojo.OssCmdMeta;
import com.x.hhqs.joa.common.util.CommonConstants;
import com.x.hhqs.joa.in.exception.ErrorKey;
import com.x.hhqs.joa.in.exception.Rt1QueryException;

public class OssCmdMetaParser {
    public static String code(OssCmdMeta ossCmdMeta) {
        return getValFromNbiMetadata(ossCmdMeta, CommonConstants.CODE);
    }

    public static String onuIp(OssCmdMeta ossCmdMeta) {
        return getValFromNbiMetadata(ossCmdMeta, CommonConstants.ONUIP);
    }

    // ip | name, depend on user input
    public static String oltId(OssCmdMeta ossCmdMeta) {
        return getValFromNbiMetadata(ossCmdMeta, CommonConstants.OLTID);
    }

    // 1-1-1-2
    public static String ponId(OssCmdMeta ossCmdMeta) {
        return getValFromNbiMetadata(ossCmdMeta, CommonConstants.PONID);
    }

    public static String ponIdVal(OssCmdMeta ossCmdMeta) {
        return "1-1-" + slotNo(ossCmdMeta) + "-" + ponNo(ossCmdMeta);
    }

    public static String onuId(OssCmdMeta ossCmdMeta) {
        return getValFromNbiMetadata(ossCmdMeta, CommonConstants.ONUID);
    }

    // slotNo from 1-1-1-2
    public static String slotNo(OssCmdMeta ossCmdMeta) {
        return ossCmdMeta.getSlotNo();
    }

    // ponNo from 1-1-1-2
    public static String ponNo(OssCmdMeta ossCmdMeta) {
        return ossCmdMeta.getPonNo();
    }

    // ONUPORT=NA-NA-1-1
    public static String onuPort(OssCmdMeta ossCmdMeta) {
        return getValFromNbiMetadata(ossCmdMeta, CommonConstants.ONUPORT);
    }

    // parse 1 from ONUPORT=NA-NA-1-2
    public static String onuCardNo(OssCmdMeta ossCmdMeta) {
        return onuPort(ossCmdMeta).split("-")[2];
    }

    // parse 2 from ONUPORT=NA-NA-1-2
    public static String onuPortNo(OssCmdMeta ossCmdMeta) {
        return onuPort(ossCmdMeta).split("-")[3];
    }

    // PORTID=NA-NA-14-1
    public static String portId(OssCmdMeta ossCmdMeta) {
        return getValFromNbiMetadata(ossCmdMeta, CommonConstants.PORTID);
    }

    // parse 1 from PORTID=NA-NA-1-2
    public static String portSlotNo(OssCmdMeta ossCmdMeta){
        return portId(ossCmdMeta).split("-")[2];
    }

    // parse 2 from PORTID=NA-NA-1-2
    public static String portPortNo(OssCmdMeta ossCmdMeta){
        return portId(ossCmdMeta).split("-")[3];
    }

    public static String onuIdType(OssCmdMeta ossCmdMeta) {
        return getValFromNbiMetadata(ossCmdMeta, CommonConstants.ONUIDTYPE);
    }

    public static String cTag(OssCmdMeta ossCmdMeta) {
        return getValFromNbiMetadata(ossCmdMeta, CommonConstants.CTAG);
    }

    public static String oltTarget(OssCmdMeta ossCmdMeta) {
        return getValFromSbiMetadata(ossCmdMeta, CommonConstants.OLT_TARGETNAME);
    }

    public static String fiberTarget(OssCmdMeta ossCmdMeta) {
        return getValFromSbiMetadata(ossCmdMeta, CommonConstants.PON_TARGETNAME);
    }

    public static String ontTarget(OssCmdMeta ossCmdMeta) {
        return getValFromSbiMetadata(ossCmdMeta, CommonConstants.ONU_TARGETNAME);
    }

    public static String oltIpVal(OssCmdMeta ossCmdMeta){
        return ossCmdMeta.getOltIP();
    }

    public static String onuIpVal(OssCmdMeta ossCmdMeta){
        return ossCmdMeta.getOnuIP();
    }

    public static String ponType(OssCmdMeta ossCmdMeta) {
        return ossCmdMeta.getPonType();
    }

    public static String fiberType(OssCmdMeta ossCmdMeta) throws Rt1QueryException {
        String ponType = ponType(ossCmdMeta);
        if ("gpon".equals(ponType)) {
            return "GPON";
        } else if ("mpm-gpon-xgs".equals(ponType)) {
            return "XGS";
        } else {
            throw new Rt1QueryException(ErrorKey.INVALID_PON_TYPE, new String[]{ponType});
        }
    }

    public static String vlan(OssCmdMeta ossCmdMeta) {
        return getValFromNbiMetadata(ossCmdMeta, "VLAN");
    }

    public static boolean isEntireNetworkCmd(OssCmdMeta ossCmdMeta) {
        return !StringUtils.hasText(oltId(ossCmdMeta)) && !StringUtils.hasText(ponId(ossCmdMeta))
                && !StringUtils.hasText(onuIdType(ossCmdMeta)) && !StringUtils.hasText(onuId(ossCmdMeta)) && !isOnuCmd(ossCmdMeta);
    }

    public static boolean isOltCmd(OssCmdMeta ossCmdMeta) {
        return !StringUtils.isEmpty(oltId(ossCmdMeta)) && StringUtils.isEmpty(ponId(ossCmdMeta))
                && StringUtils.isEmpty(onuIdType(ossCmdMeta)) && StringUtils.isEmpty(onuId(ossCmdMeta));
    }

    public static boolean isPonCmd(OssCmdMeta ossCmdMeta) {
        return !isOnuCmd(ossCmdMeta) && !StringUtils.isEmpty(ponId(ossCmdMeta));
    }

    public static boolean isOnuCmd(OssCmdMeta ossCmdMeta) {
        if (StringUtils.hasText(onuIp(ossCmdMeta))) {
            return true;
        }
        return !StringUtils.isEmpty(onuIdType(ossCmdMeta)) && !StringUtils.isEmpty(onuId(ossCmdMeta));
    }

    public static boolean isOnuPortCmd(OssCmdMeta ossCmdMeta) {
        return isOnuCmd(ossCmdMeta) && !StringUtils.isEmpty(onuPort(ossCmdMeta));
    }

    public static boolean isOnuPortIdCmd(OssCmdMeta ossCmdMeta) {
        return isOnuCmd(ossCmdMeta) && !StringUtils.isEmpty(portId(ossCmdMeta));
    }


    public static boolean isPortIdCmd(OssCmdMeta ossCmdMeta) {
        return !StringUtils.isEmpty(portId(ossCmdMeta));
    }

    public static boolean isVlanDesignated(OssCmdMeta ossCmdMeta) {
        return !StringUtils.isEmpty(vlan(ossCmdMeta));
    }

    public static String getValFromNbiMetadata(OssCmdMeta ossCmdMeta, String key) {
        return ossCmdMeta.getNbiMetadata().get(key);
    }

    public static String getValFromSbiMetadata(OssCmdMeta ossCmdMeta, String key) {
        return ossCmdMeta.getSbiMetadata().get(key);
    }

}
