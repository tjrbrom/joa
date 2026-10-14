package com.x.hhqs.joa.sa.model;

import com.x.hhqs.joa.common.rt1fwk.exception.OssCommandException;
import com.x.hhqs.joa.sa.netconf.NetconfRequest;

import java.util.Map;

public interface ModelDataProcessor {
    void process(NetconfRequest.Data data, Map<String, Object> model) throws OssCommandException;
}
