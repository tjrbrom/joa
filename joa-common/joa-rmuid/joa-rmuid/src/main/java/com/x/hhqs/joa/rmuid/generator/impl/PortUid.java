package com.x.hhqs.joa.rmuid.generator.impl;

import org.springframework.util.StringUtils;
import com.x.hhqs.joa.rmuid.api.QueryService;
import com.x.hhqs.joa.rmuid.generator.AbstractRmUid;
import com.x.hhqs.joa.rmuid.generator.OmcUid;
import com.x.hhqs.joa.rmuid.generator.RmType;
import com.x.hhqs.joa.rmuid.model.HamObjectId;
import com.x.hhqs.joa.rmuid.model.Pon;
import com.x.hhqs.joa.rmuid.util.HamObjectUtil;

public class PortUid extends AbstractRmUid {

    public PortUid(OmcUid omcUid, QueryService queryService) {
        super(omcUid, queryService);
    }

    // @formatter:off
    /**
     * 1) uplink 
     *    /device=<device-name>/slot=<slot>/eth=<port>
     * @param hamObjectId: "/device=test01/slot=NT-A/eth=ETH-1"
     * @return NTA01
     * 
     * @param hamObjectId: "/device=test01/slot=NTIO-1/eth=PORT-1:1"
     * @return NTIO10101
     * 
     * 2) PON
     * @param hamObjectId: /device=<device-name>/slot=<slot>/pon=<pon>
     * @return 2(SLOT)2(PON)
     * 
     * 3) PON Link
     * @param hamObjectId: /device=<device-name>/slot=<slot>/pon=<pon>/pon-link=<fiber-name>
     * @return same as PON
     * 
     * 4) Cage
     * @param hamObjectId: /device=<device-name>/slot= LT-<n>/cage=<m>
     * @return 2(SLOT)2(CAGE)
     * 
     * 5) uni
     * @param hamObjectId:
     *        "/device=<device-name>/slot=LT-<slot-position>/pon=PON-<pon-position>-<technology>/ont=<ont-name>/uni=<uni-name>"
     * @return 2(SLOT)1(PON-technology)2(PON)3(ONUID)1(uni-sign)2(uni-port) & radix 36
     * 
     * 6) onu uplink
     * hamObjectId: "/device=test01/slot=LT-4/pon=PON-1-XGS/ont=ontName"
     * @return 2(SLOT)1(PON-technology)2(PON)3(ONUID)
     */
     // @formatter:on
    @Override
    public String generateUidById(HamObjectId hamObjectId) {
        String slot = HamObjectUtil.getSlot(hamObjectId).toUpperCase();
        // uplink
        if (slot.startsWith("NT")) {
            return slot + getEthPort(hamObjectId);
        }

        // UNI
        if (HamObjectUtil.isPrefixUni(hamObjectId)) {
            int slotId = HamObjectUtil.getLtNumber(hamObjectId);
            Pon pon = HamObjectUtil.getPon(hamObjectId);

            int onuId = getOnuId(hamObjectId);
            int uniCode = HamObjectUtil.parseUni(hamObjectId.getValue("uni")).toCode();

            String value = String.format("%02d%d%02d%03d%02d", slotId, pon.getTechnology().getValue(),
                pon.getId(), onuId, uniCode);

            return Long.toString(Long.valueOf(value), 36);
        }
        // ONU uplink
        else if (HamObjectUtil.isOnt(hamObjectId) || HamObjectUtil.isOntId(hamObjectId)) {
            int onuId = getOnuId(hamObjectId);
            int slotId = HamObjectUtil.getLtNumber(hamObjectId);
            Pon pon = HamObjectUtil.getPon(hamObjectId);

            return String.format("%02d%d%02d%03d", slotId, pon.getTechnology().getValue(),
                pon.getId(), onuId);
        }
        // PON or PON Link
        else if (HamObjectUtil.isPrefixPon(hamObjectId)) {
            int slotId = HamObjectUtil.getLtNumber(hamObjectId);
            Pon pon = HamObjectUtil.getPon(hamObjectId);
            return String.format("%02d%02d", slotId, pon.getId());
        }
        // Cage
        else if (HamObjectUtil.isPrefixCage(hamObjectId)) {
            int slotId = HamObjectUtil.getLtNumber(hamObjectId);
            int cage = Integer.valueOf(hamObjectId.getValue("cage"));
            return String.format("%02d%02d", slotId, cage);
        }
        

        throw new IllegalArgumentException("Invalid Port: " + hamObjectId);
    }

    String getEthPort(HamObjectId hamObjectId) {
        String eth = hamObjectId.getValue("eth");
        if (!StringUtils.hasText(eth)) {
            throw new IllegalArgumentException("Invalid Port: " + hamObjectId);
        }

        int index = eth.indexOf("-");
        if (index != -1) {
            eth = eth.substring(index + 1);
        }

        String[] parts = eth.split("[:]");
        if (parts.length == 1) {
            return String.format("%02d", Integer.valueOf(parts[0]));
        } else if (parts.length == 2) {
            return String.format("%02d%02d", Integer.valueOf(parts[0]), Integer.valueOf(parts[1]));
        }

        throw new IllegalArgumentException("Invalid Port: " + hamObjectId);
    }

    @Override
    public RmType getRmType() {
        return RmType.Port;
    }

}
