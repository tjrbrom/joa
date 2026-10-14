package com.x.hhqs.joa.rmuid.generator.impl;

import com.x.hhqs.joa.rmuid.api.QueryService;
import com.x.hhqs.joa.rmuid.generator.AbstractRmUid;
import com.x.hhqs.joa.rmuid.generator.OmcUid;
import com.x.hhqs.joa.rmuid.generator.RmType;
import com.x.hhqs.joa.rmuid.model.HamObjectId;
import com.x.hhqs.joa.rmuid.util.HamObjectUtil;

public class HolderUid extends AbstractRmUid {

    public HolderUid(OmcUid omcUid, QueryService queryService) {
        super(omcUid, queryService);
    }

    /**
     * <pre>
     * +-----------+------------------------------------------------+    +-------------+
     * | type      | hamObjectId                                    |--->| uid         |
     * | rack      | /device=test01/rack=R1                         |    | R1          |
     * | shelf     | /device=test01/rack=R1/subrack=S1              |    | S1          |
     * | NTA       | /device=test01/slot=NT-A                       |    | NTA         |
     * | NTB       | /device=test01/slot=NT-B                       |    | NTB         |
     * | ACU       | /device=test01/slot=ACU                        |    | ACU         |
     * | NTIO-A    | /device=test01/slot=NTIO-1                     |    | NTIO1       |
     * | NTIO-B    | /device=test01/slot=NTIO-2                     |    | NTIO2       |
     * | LT        | /device=test01/slot=LT-1                       |    | LT1         |
     * +-----------+------------------------------------------------+    +-------------+
     * </pre>
     */
    @Override
    public String generateUidById(HamObjectId hamObjectId) {
        if (hamObjectId.hasRdn("subrack")) {
            return hamObjectId.getValue("subrack");
        } else if (hamObjectId.hasRdn("rack")) {
            return hamObjectId.getValue("rack");
        } else {
            return HamObjectUtil.getSlot(hamObjectId);
        }

    }

    @Override
    public RmType getRmType() {
        return RmType.Holder;
    }
}
