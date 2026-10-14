package com.x.hhqs.joa.rmuid.service;

import com.x.hhqs.joa.rmuid.generator.OmcUid;
import com.x.hhqs.joa.rmuid.generator.RmType;
import com.x.hhqs.joa.rmuid.generator.RmUidGenerator;
import com.x.hhqs.joa.rmuid.generator.RmUidGeneratorManager;
import com.x.hhqs.joa.rmuid.model.HamObject;
import com.x.hhqs.joa.rmuid.model.HamObjectId;

public class RmUidService {

    private final RmUidGeneratorManager uidGeneratorManager;

    private final OmcUid omcUid;

    public RmUidService(RmUidGeneratorManager uidGeneratorManager, OmcUid omcUid) {
        this.uidGeneratorManager = uidGeneratorManager;
        this.omcUid = omcUid;
    }

    public String getOmcRmUid() {
        return omcUid.getRmUid();
    }

    public String generateRmuid(HamObject hamObject) throws Exception {
        HamObjectId hamObjectId = new HamObjectId(hamObject.getHamId());

        RmUidGenerator generator = uidGeneratorManager.getRmUidGenerator(hamObject.getResourceType());
        if (generator == null) {
            throw new IllegalArgumentException("Unsupported resourceType: " + hamObject.getResourceType());
        }

        return generator.generateRmUidById(hamObjectId);
    }

    public String generateRmuid(String hamId, String resourceType) {
        HamObjectId hamObjectId = new HamObjectId(hamId);

        RmUidGenerator generator = uidGeneratorManager.getRmUidGenerator(resourceType);
        if (generator == null) {
            throw new IllegalArgumentException("Unsupported resourceType: " + resourceType);
        }

        return generator.generateRmUidById(hamObjectId);
    }

    public String generateRmuid(String hamId, RmType rmType) {
        HamObjectId hamObjectId = new HamObjectId(hamId);

        RmUidGenerator generator = uidGeneratorManager.getRmUidGenerator(rmType.getValue());
        if (generator == null) {
            throw new IllegalArgumentException("Unsupported resourceType: " + rmType.getValue());
        }

        return generator.generateRmUidById(hamObjectId);
    }

}
