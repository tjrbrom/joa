package com.x.hhqs.joa.rmuid.generator.impl;

import com.x.hhqs.joa.rmuid.api.QueryService;
import com.x.hhqs.joa.rmuid.generator.AbstractRmUid;
import com.x.hhqs.joa.rmuid.generator.OmcUid;
import com.x.hhqs.joa.rmuid.generator.RmType;
import com.x.hhqs.joa.rmuid.model.HamObjectId;
import com.x.hhqs.joa.rmuid.model.Pon;
import com.x.hhqs.joa.rmuid.util.HamObjectUtil;

public class OnuUid extends AbstractRmUid {

    public OnuUid(OmcUid omcUid, QueryService queryService) {
        super(omcUid, queryService);
    }

    /**
     * @param hamObjectId: "/device=test01/slot=LT-4/pon=PON-1-XGS/ont=ontName"
     * @return 2(SLOT)1(PON-technology)2(PON)3(ONUID) --> "04201001"
     */
    public String generateUidById(HamObjectId hamObjectId) {
        int onuId = getOnuId(hamObjectId);
        int slotId = HamObjectUtil.getLtNumber(hamObjectId);
        Pon pon = HamObjectUtil.getPon(hamObjectId);

        return String.format("%02d%d%02d%03d", slotId, pon.getTechnology().getValue(),
            pon.getId(), onuId);
    }

    @Override
    public RmType getRmType() {
        return RmType.ONU;
    }
}
