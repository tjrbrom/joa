package com.x.hhqs.joa.rmuid.generator.impl;

import com.x.hhqs.joa.rmuid.api.QueryService;
import com.x.hhqs.joa.rmuid.generator.AbstractRmUid;
import com.x.hhqs.joa.rmuid.generator.OmcUid;
import com.x.hhqs.joa.rmuid.generator.RmType;
import com.x.hhqs.joa.rmuid.model.HamObjectId;
import com.x.hhqs.joa.rmuid.util.HamObjectUtil;

public class TopologyUid extends AbstractRmUid {

    public TopologyUid(OmcUid omcUid, QueryService queryService) {
        super(omcUid, queryService);
    }

    private OnuUid onuUid;

    private PosUid posUid;

    /**
     * @param hamObjectId:
     *        /device=<device-name>/slot=LT-<slot-position>/pon=PON-<pon-position>-<technology>/ont=<ont-name>
     *        /device=<device-name>/slot=LT-<slot-position>/pon=PON-<pon-position>-<technology>/splitter=<splitter-name>
     * @return 1(sign)2(SLOT)1(PON-technology)2(PON)3(ONU|POS)
     */
    @Override
    public String generateUidById(HamObjectId hamObjectId) {
        if (HamObjectUtil.isPrefixOnt(hamObjectId) || HamObjectUtil.isPrefixOntId(hamObjectId)) {
            return "0" + onuUid.generateUidById(hamObjectId);
        } else if (HamObjectUtil.isPos(hamObjectId)) {
            return "1" + posUid.generateUidById(hamObjectId);
        }
        throw new IllegalArgumentException("Invalid Topology: " + hamObjectId);
    }

    @Override
    public RmType getRmType() {
        return RmType.Topology;
    }

    public void setOnuUid(OnuUid onuUid) {
        this.onuUid = onuUid;
    }

    public void setPosUid(PosUid posUid) {
        this.posUid = posUid;
    }

}
