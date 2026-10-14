package com.x.hhqs.joa.rmuid.generator;

import java.util.Optional;
import org.springframework.util.StringUtils;
import com.x.hhqs.joa.rmuid.api.QueryService;
import com.x.hhqs.joa.rmuid.exception.JoaRmuidException;
import com.x.hhqs.joa.rmuid.exception.ResourceNotFoundException;
import com.x.hhqs.joa.rmuid.model.HamObjectId;
import com.x.hhqs.joa.rmuid.util.HamObjectUtil;

public abstract class AbstractRmUid implements RmUidGenerator {
    public static final int RADIX = 36;

    protected final OmcUid omcUid;

    protected final QueryService queryService;

    public AbstractRmUid(OmcUid omcUid, QueryService queryService) {
        this.omcUid = omcUid;
        this.queryService = queryService;
    }

    @Override
    public String generateRmUidById(HamObjectId hamObjectId) {
        String neUid = generateNeUid(hamObjectId);
        String uid = generateUidById(hamObjectId);
        if (uid.length() > 9) {
            throw new JoaRmuidException("Invalid uid: " + uid + ", " + hamObjectId);
        }
        return completeUid(neUid, uid);
    }

    private String completeUid(String neUid, String uid) {
        String rmUid = omcUid.getUid() + getRmType().getValue() + neUid + uid;
        return rmUid.toUpperCase();
    }

    protected String generateNeUid(HamObjectId hamObjectId) {
        String ip = hamObjectId.getValue("ip");
        if (StringUtils.hasText(ip)) {
            return NeUid.generateUid(ip);
        }

        String deviceName = hamObjectId.getValue("device");

        if (!StringUtils.hasText(deviceName)) {
            throw new IllegalArgumentException("Invalid device: " + hamObjectId);
        }

        return NeUid.generateUid(getIp(hamObjectId));
    }

    private String getIp(HamObjectId hamObjectId) {
        return queryService.getDeviceIp(hamObjectId)
            .orElseThrow(() -> new ResourceNotFoundException("Cannot get ip by device name: " + hamObjectId));
    }

    protected abstract String generateUidById(HamObjectId hamObjectId);

    public QueryService getQueryService() {
        return queryService;
    }

    public Integer getOnuId(HamObjectId hamObjectId) {
        Optional<Integer> onuId = Optional.empty();
        if (HamObjectUtil.isPrefixOntId(hamObjectId)) {
            onuId = Optional.ofNullable(hamObjectId.getValue("ontid"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::valueOf);
        } else if (HamObjectUtil.isPrefixOnt(hamObjectId)) {
            onuId = getQueryService().getOnuId(hamObjectId);
        } else {
            throw new IllegalArgumentException("Invalid ONT: " + hamObjectId);
        }

        if (onuId.isEmpty()) {
            throw new ResourceNotFoundException("Cannot find ONT id: " + hamObjectId);
        }

        return onuId.get();
    }
}
