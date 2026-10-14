package com.x.hhqs.joa.rmuid.generator.impl;

import com.x.hhqs.joa.rmuid.api.QueryService;
import com.x.hhqs.joa.rmuid.generator.AbstractRmUid;
import com.x.hhqs.joa.rmuid.generator.OmcUid;
import com.x.hhqs.joa.rmuid.generator.RmType;
import com.x.hhqs.joa.rmuid.model.HamObjectId;

public class OltUid extends AbstractRmUid {

    public OltUid(OmcUid omcUid, QueryService queryService) {
        super(omcUid, queryService);
    }

    @Override
    public String generateUidById(HamObjectId hamObjectId) {
        return "";
    }

    @Override
    public RmType getRmType() {
        return RmType.OLT;
    }

}
