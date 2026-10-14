package com.x.hhqs.joa.rmuid.generator.impl;

import com.x.hhqs.joa.rmuid.api.QueryService;
import com.x.hhqs.joa.rmuid.generator.AbstractRmUid;
import com.x.hhqs.joa.rmuid.generator.OmcUid;
import com.x.hhqs.joa.rmuid.generator.RmType;
import com.x.hhqs.joa.rmuid.model.HamObjectId;

public class LagUid extends AbstractRmUid {

    public LagUid(OmcUid omcUid, QueryService queryService) {
        super(omcUid, queryService);
    }

    /**
     * @param hamObjectId: /device=<device-name>/lag=LAG-<lag-index>
     * @return LAG<lag-index>
     */
    @Override
    public String generateUidById(HamObjectId hamObjectId) {
        String lag = hamObjectId.getValue("lag");
        if (lag == null || lag.isBlank()) {
            throw new IllegalArgumentException("Invalid LAG: " + hamObjectId);
        }

        return lag.replace("-", "");
    }

    @Override
    public RmType getRmType() {
        return RmType.LAG;
    }
}
