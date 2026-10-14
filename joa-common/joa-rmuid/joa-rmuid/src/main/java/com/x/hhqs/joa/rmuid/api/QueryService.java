package com.x.hhqs.joa.rmuid.api;

import java.util.Optional;
import com.x.hhqs.joa.rmuid.model.HamObjectId;

public interface QueryService {

    Optional<String> getDeviceIp(HamObjectId hamObjectId);

    Optional<Integer> getOnuId(HamObjectId hamObjectId);

}
