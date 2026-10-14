package com.x.hhqs.joa.rmuid.generator.impl;

import com.x.hhqs.joa.rmuid.api.QueryService;
import com.x.hhqs.joa.rmuid.generator.AbstractRmUid;
import com.x.hhqs.joa.rmuid.generator.OmcUid;
import com.x.hhqs.joa.rmuid.generator.RmType;
import com.x.hhqs.joa.rmuid.model.HamObjectId;
import com.x.hhqs.joa.rmuid.util.HamObjectUtil;

public class BoardUid extends AbstractRmUid {

    private OnuUid onuUid;

    public BoardUid(OmcUid omcUid, QueryService queryService) {
        super(omcUid, queryService);
    }

    /**
     * <pre>
     * +-----------+------------------------------------------------+    +-------------+
     * | type      | hamObjectId                                    |--->| uid         |
     * | NTA       | /device=test01/slot=NT-A                       |    | NTA         |
     * | NTB       | /device=test01/slot=NT-B                       |    | NTB         |
     * | ACU       | /device=test01/slot=ACU                        |    | ACU         |
     * | NTIO-A    | /device=test01/slot=NTIO-1                     |    | NTIO1       |
     * | NTIO-B    | /device=test01/slot=NTIO-2                     |    | NTIO2       |
     * | LT        | /device=test01/slot=LT-1                       |    | LT1         |
     * +-----------+------------------------------------------------+    +-------------+
     * 
     * ONU SLOT: /device=test01/slot=LT-4/pon=PON-1-XGS/ont=ontName/ontSlot=1 
     *           2(SLOT)1(PON-technology)2(PON)3(ONUID)2(ONUSLOT) --> "0420100101" & radix 36
     * </pre>
     */
    @Override
    public String generateUidById(HamObjectId hamObjectId) {
        if(hamObjectId.hasRdn("ontSlot")) {
            String ontSlot = String.format("%02d", Integer.parseInt(hamObjectId.getValue("ontSlot")));
            String value = onuUid.generateUidById(hamObjectId) + ontSlot;
            
            return Long.toString(Long.valueOf(value), 36);
        }
        
        return HamObjectUtil.getSlot(hamObjectId);
    }
    
    @Override
    public RmType getRmType() {
        return RmType.Board;
    }

    public void setOnuUid(OnuUid onuUid) {
        this.onuUid = onuUid;
    }
}
