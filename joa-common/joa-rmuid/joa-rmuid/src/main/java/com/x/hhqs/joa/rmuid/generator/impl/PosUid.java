package com.x.hhqs.joa.rmuid.generator.impl;

import com.x.hhqs.joa.rmuid.api.QueryService;
import com.x.hhqs.joa.rmuid.generator.AbstractRmUid;
import com.x.hhqs.joa.rmuid.generator.OmcUid;
import com.x.hhqs.joa.rmuid.generator.RmType;
import com.x.hhqs.joa.rmuid.model.HamObjectId;
import com.x.hhqs.joa.rmuid.model.Pon;
import com.x.hhqs.joa.rmuid.util.HamObjectUtil;

public class PosUid extends AbstractRmUid {

    public PosUid(OmcUid omcUid, QueryService queryService) {
        super(omcUid, queryService);
    }

    /**
     * @param hamObjectId:
     *        /device=<device-name>/slot=LT-<slot-position>/pon=PON-<pon-position>-<technology>/splitter=<splitter-name>
     *        e.g. hamObjectId: "/device=test01/slot=LT-4/pon=PON-1-XGS/splitter=fibername_001"
     * @return 2(SLOT)1(PON-technology)2(PON)3(POS) --> "04201001"
     */
    @Override
    public String generateUidById(HamObjectId hamObjectId) {
        if (!HamObjectUtil.isPos(hamObjectId)) {
            throw new IllegalArgumentException("Invalid POS: " + hamObjectId);
        }

        int slotId = HamObjectUtil.getLtNumber(hamObjectId);
        Pon pon = HamObjectUtil.getPon(hamObjectId);
        int posId = HamObjectUtil.getSplitterId(hamObjectId);

        return String.format("%02d%d%02d%03d", slotId, pon.getTechnology().getValue(),
            pon.getId(), posId);
    }

    @Override
    public RmType getRmType() {
        return RmType.POS;
    }

}
