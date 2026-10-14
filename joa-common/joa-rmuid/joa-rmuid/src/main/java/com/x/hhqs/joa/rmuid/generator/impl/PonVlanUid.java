package com.x.hhqs.joa.rmuid.generator.impl;

import com.x.hhqs.joa.rmuid.api.QueryService;
import com.x.hhqs.joa.rmuid.generator.AbstractRmUid;
import com.x.hhqs.joa.rmuid.generator.OmcUid;
import com.x.hhqs.joa.rmuid.generator.RmType;
import com.x.hhqs.joa.rmuid.model.HamObjectId;
import com.x.hhqs.joa.rmuid.model.InnerFriendlyName;
import com.x.hhqs.joa.rmuid.util.HamObjectUtil;

public class PonVlanUid extends AbstractRmUid {

    public PonVlanUid(OmcUid omcUid, QueryService queryService) {
        super(omcUid, queryService);
    }

    // @formatter:off
    /**
     * @param hamObjectId: /device=<device-name>/vlan=[S<s-vlan-id>].C<c-vlan-id>
     * @return 4(SVLAN)4(CVLAN)
     */
     // @formatter:on
    @Override
    public String generateUidById(HamObjectId hamObjectId) {
        if (HamObjectUtil.isPrefixPonVlan(hamObjectId)) {
            String vlan = hamObjectId.getValue("vlan");
            InnerFriendlyName ifn = new InnerFriendlyName(vlan);

            Long sVlanId = ifn.getValue("S");
            if (sVlanId == null)
                sVlanId = 0l;
            Long cVlanId = ifn.getValue("C");
            if (cVlanId == null)
                cVlanId = 0l;

            return String.format("%04d%04d", sVlanId, cVlanId);
        }

        throw new IllegalArgumentException("Invalid PonVlan: " + hamObjectId);
    }

    @Override
    public RmType getRmType() {
        return RmType.PonVLAN;
    }

}

