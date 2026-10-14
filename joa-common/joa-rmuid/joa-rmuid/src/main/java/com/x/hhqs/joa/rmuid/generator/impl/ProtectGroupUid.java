package com.x.hhqs.joa.rmuid.generator.impl;

import com.x.hhqs.joa.rmuid.api.QueryService;
import com.x.hhqs.joa.rmuid.generator.AbstractRmUid;
import com.x.hhqs.joa.rmuid.generator.OmcUid;
import com.x.hhqs.joa.rmuid.generator.RmType;
import com.x.hhqs.joa.rmuid.model.HamObjectId;
import com.x.hhqs.joa.rmuid.model.Pon;
import com.x.hhqs.joa.rmuid.util.HamObjectUtil;

public class ProtectGroupUid extends AbstractRmUid {

    public ProtectGroupUid(OmcUid omcUid, QueryService queryService) {
        super(omcUid, queryService);
    }

    // @formatter:off
    /**
     * @param hamObjectId: /device=<device-name>/slot=<slot>/pon=<pon>
     * @return 2(SLOT)2(PON)
     */
     // @formatter:on
    @Override
    public String generateUidById(HamObjectId hamObjectId) {
        if (HamObjectUtil.isPrefixPon(hamObjectId)) {
            int slotId = HamObjectUtil.getLtNumber(hamObjectId);
            Pon pon = HamObjectUtil.getPon(hamObjectId);
            return String.format("%02d%02d", slotId, pon.getId());
        }

        throw new IllegalArgumentException("Invalid ProtectGroup: " + hamObjectId);
    }

    @Override
    public RmType getRmType() {
        return RmType.ProtectGroup;
    }

}
