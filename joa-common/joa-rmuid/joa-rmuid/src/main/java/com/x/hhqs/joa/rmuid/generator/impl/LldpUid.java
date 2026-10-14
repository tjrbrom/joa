package com.x.hhqs.joa.rmuid.generator.impl;

import java.util.Map;
import java.util.TreeMap;
import com.x.hhqs.joa.rmuid.api.QueryService;
import com.x.hhqs.joa.rmuid.generator.AbstractRmUid;
import com.x.hhqs.joa.rmuid.generator.OmcUid;
import com.x.hhqs.joa.rmuid.generator.RmType;
import com.x.hhqs.joa.rmuid.model.HamObjectId;
import com.x.hhqs.joa.rmuid.util.HamObjectUtil;

public class LldpUid extends AbstractRmUid {
    private static final Map<String, Integer> addrMap;

    static {
        addrMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        addrMap.put("Nearest_Bridge", 1);
        addrMap.put("Nearest-Bridge", 1);
        addrMap.put("Nearest_Non_TPMR", 2);
        addrMap.put("Nearest-Non-TPMR", 2);
        addrMap.put("Nearest-NonTPMR", 2);
        addrMap.put("Nearest_Customer", 3);
        addrMap.put("Nearest-Customer", 3);
    }

    private static final Map<String, Integer> slotMap;

    static {
        slotMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        slotMap.put("NTA", 1);
        slotMap.put("NT-A", 1);
        slotMap.put("NTB", 2);
        slotMap.put("NT-B", 2);
        slotMap.put("NTIO-1", 3);
        slotMap.put("NTIO1", 3);
        slotMap.put("NTIO-2", 4);
        slotMap.put("NTIO2", 4);
    }


    public LldpUid(OmcUid omcUid, QueryService queryService) {
        super(omcUid, queryService);
    }

    // @formatter:off
    /**
     * @param hamObjectId: /device=<device-name>/slot=NT-B/eth=PORT-1:3/addrNr=nearest-bridge/timeMarkNr=1772432248/remNr=1
     * @return 
     */
     // @formatter:on
    @Override
    public String generateUidById(HamObjectId hamObjectId) {
        if (HamObjectUtil.isLldp(hamObjectId)) {
            Integer slot = slotMap.get(hamObjectId.getValue("slot"));
            if (slot == null) {
                throw new IllegalArgumentException("Invalid LLDP: " + hamObjectId);
            }

            String eth = getEthPort(hamObjectId.getValue("eth"));

            Integer addr = addrMap.get(hamObjectId.getValue("addrNr"));
            if (addr == null) {
                throw new IllegalArgumentException("Invalid LLDP: " + hamObjectId);
            }

            String remId = Long.toString(Long.valueOf(hamObjectId.getValue("remNr")), 36);
            remId = "0".repeat(Math.max(0, 6 - remId.length())) + remId;

            Long id = Long.valueOf("" + slot + eth + addr);
            return Long.toString(id, 36) + remId;
        }

        throw new IllegalArgumentException("Invalid LLDP: " + hamObjectId);
    }

    public String getEthPort(String input) {
        String part = input.substring(input.indexOf("-") + 1);

        char firstDigit = part.charAt(0);
        if (part.length() > 1) {
            if (part.charAt(1) == ':') {
                return "" + firstDigit + part.charAt(2);
            }
        }

        return "" + firstDigit + "0";
    }

    @Override
    public RmType getRmType() {
        return RmType.LLDP;
    }

}


