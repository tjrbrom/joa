package com.x.hhqs.joa.rmuid.generator;

import com.x.hhqs.joa.rmuid.config.OmcConfig;
import com.x.hhqs.joa.rmuid.model.HamObjectId;

public class OmcUid implements RmUidGenerator {
    public static final String DEFAULT_OMC_UID = "";// 0000ASCS1

    private final OmcConfig omcConfig;

    public OmcUid(OmcConfig omcConfig) {
        this.omcConfig = omcConfig;
    }

    public String getUid() {
        return omcConfig.getCity() + omcConfig.getVendor()
            + omcConfig.getCategory() + omcConfig.getNumber();
    }
    
    public String getRmUid() {
        return getUid() + getRmType().getValue();
    }

    @Override
    public String generateRmUidById(HamObjectId hamObjectId) {
        return getRmUid();
    }

    @Override
    public RmType getRmType() {
        return RmType.OMC;
    }

}
