package com.x.hhqs.joa.rmuid.generator;

import com.x.hhqs.joa.rmuid.model.HamObjectId;

public interface RmUidGenerator {

    String generateRmUidById(HamObjectId hamObjectId);

    RmType getRmType();

}
