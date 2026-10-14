package com.x.hhqs.joa.rmuid.generator;

import java.text.SimpleDateFormat;
import java.util.Date;
import com.x.hhqs.joa.rmuid.util.DateUtil;

public class NeUid {
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final int RADIX = 36;
    private static final long TIME_FIX = 20000000000l;

    private static NeUid neUid = new NeUid();

    private NeUid() {
        //
    }

    public static String parseUid(String uid) {
        long s = Long.valueOf(uid, RADIX);
        if (s > TIME_FIX) {
            return neUid.longToDateTime(s);
        }

        return neUid.longToIp(s);
    }

    private String longToDateTime(long s) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        return sdf.format(new Date((s - TIME_FIX) * 1000l));
    }

    // rmuidSource: datetime or ipAddress
    public static String generateUid(String rmuidSource) {
        try {
            if (DateUtil.isLegalDate(rmuidSource)) {
                return neUid.generateUidByDateTime(rmuidSource);
            } else {
                return neUid.generateUidByIpAddress(rmuidSource);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("invalid Ne rmuidSource:" + rmuidSource);
        }
    }

    // yyyy-MM-dd HH:mm:ss
    private String generateUidByDateTime(String datetime) {
        Date date = DateUtil.parseDate(datetime);
        long s = date.getTime() / 1000l;
        s = s + TIME_FIX;

        return String.format("%7s", Long.toString(s, RADIX)).replace(' ', '0').toUpperCase();
    }

    private String generateUidByIpAddress(String ipAddress) {
        String s = Long.toString(ipToLong(ipAddress), RADIX);
        return String.format("%7s", s).replace(' ', '0').toUpperCase();
    }

    private long ipToLong(String ipAddress) {
        if (ipAddress == null || ipAddress.isEmpty()) {
            throw new IllegalArgumentException("invalid Ne rmuidSource:" + ipAddress);
        }
        String[] octets = ipAddress.split(java.util.regex.Pattern.quote("."));
        if (octets.length != 4) {
            throw new IllegalArgumentException("invalid Ne rmuidSource:" + ipAddress);
        }
        long ip = 0;
        for (int i = 3; i >= 0; i--) {
            long octet = Long.parseLong(octets[3 - i]);
            if (octet > 255 || octet < 0) {
                throw new IllegalArgumentException("invalid Ne rmuidSource:" + ipAddress);
            }
            ip |= octet << (i * 8);
        }
        return ip;
    }

    private String longToIp(long ip) {
        if (ip > 4294967295l || ip < 0) {
            throw new IllegalArgumentException("invalid ip");
        }

        StringBuilder sb = new StringBuilder(15);

        for (int i = 0; i < 4; i++) {
            sb.insert(0, Long.toString(ip & 0xff));
            if (i < 3) {
                sb.insert(0, '.');
            }
            ip >>= 8;
        }
        return sb.toString();
    }

}
