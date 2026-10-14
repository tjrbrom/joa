package com.x.hhqs.joa.rmuid.generator.impl;

import com.x.hhqs.joa.rmuid.api.QueryService;
import com.x.hhqs.joa.rmuid.generator.AbstractRmUid;
import com.x.hhqs.joa.rmuid.generator.OmcUid;
import com.x.hhqs.joa.rmuid.generator.RmType;
import com.x.hhqs.joa.rmuid.model.HamObjectId;
import com.x.hhqs.joa.rmuid.util.HamObjectUtil;

public class SrfUid extends AbstractRmUid {

    public SrfUid(OmcUid omcUid, QueryService queryService) {
        super(omcUid, queryService);
    }

    /**
     * <pre>
     * +-----------+------------------------------------------------+    +-------------+
     * | type      | hamObjectId                                    |--->| uid         |
     * |           | /device=test01                                 |    |             |
     * | NTA       | /device=test01/slot=NT-A                       |    | NTA         |
     * | NTB       | /device=test01/slot=NT-B                       |    | NTB         |
     * | ACU       | /device=test01/slot=ACU                        |    | ACU         |
     * | NTIO-A    | /device=test01/slot=NTIO-1                     |    | NTIO1       |
     * | NTIO-B    | /device=test01/slot=NTIO-1                     |    | NTIO1       |
     * | LT        | /device=test01/slot=LT-1                       |    | LT1         |
     * +-----------+------------------------------------------------+    +-------------+
     * </pre>
     */
    @Override
    public String generateUidById(HamObjectId hamObjectId) {
        if (!hamObjectId.hasRdn("slot")) {
            return "";
        }

        return HamObjectUtil.getSlot(hamObjectId);
    }

    @Override
    public RmType getRmType() {
        return RmType.SFR;
    }
}
