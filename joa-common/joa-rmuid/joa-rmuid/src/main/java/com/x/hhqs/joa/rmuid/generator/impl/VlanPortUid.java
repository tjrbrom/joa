package com.x.hhqs.joa.rmuid.generator.impl;

import com.x.hhqs.joa.rmuid.api.QueryService;
import com.x.hhqs.joa.rmuid.generator.AbstractRmUid;
import com.x.hhqs.joa.rmuid.generator.OmcUid;
import com.x.hhqs.joa.rmuid.generator.RmType;
import com.x.hhqs.joa.rmuid.model.HamObjectId;
import com.x.hhqs.joa.rmuid.model.Pon;
import com.x.hhqs.joa.rmuid.util.HamObjectUtil;

public class VlanPortUid extends AbstractRmUid {

    public VlanPortUid(OmcUid omcUid, QueryService queryService) {
        super(omcUid, queryService);
    }

    /**
     * @param hamObjectId:
     *        "/device=<device-name>/slot=LT-<slot-position>/pon=PON-<pon-position>-<technology>/ont=<ont-name>/uni=<uni-name>/service=<service-name>"
     *        "/device=test01/slot=LT-1/pon=PON-2-XGS/ont=2f5do1rzdl/uni=VOIP-SIP/service=VSI_voip1_2f5do1rzdl_VOIP_1234.300%3A300"
     * @return VlanPortUid
     */
    @Override
    public String generateUidById(HamObjectId hamObjectId) {
        if (HamObjectUtil.isVlanPort(hamObjectId)) {
            int slotId = HamObjectUtil.getLtNumber(hamObjectId);
            if (slotId < 1 || slotId > 16) {
                throw new IllegalArgumentException("slot out of range (1-16)");
            }

            Pon pon = HamObjectUtil.getPon(hamObjectId);
            int ponCode = (pon.getTechnology().getValue() << 5) | (pon.getId() - 1);

            int onuId = getOnuId(hamObjectId);
            if (onuId < 0 || onuId > 255) {
                throw new IllegalArgumentException("onuId out of range (0-255)");
            }

            int uniCode = HamObjectUtil.parseUni(hamObjectId.getValue("uni")).toCode();
            int serviceCode = HamObjectUtil.parseVLanService(hamObjectId.getValue("service")).toCode();

            long value = encode(slotId, ponCode, onuId, uniCode, serviceCode);
            return Long.toString(value, 36);
        }

        throw new IllegalArgumentException("Invalid Port: " + hamObjectId);
    }

    public static long encode(long slotId, long ponCode, long onuId, long uniCode, long serviceCode) {
        return (serviceCode << 26) | (uniCode << 20) | (onuId << 12) | (ponCode << 4) | (slotId - 1);
    }

    @Override
    public RmType getRmType() {
        return RmType.PtpToVLAN;
    }
}
