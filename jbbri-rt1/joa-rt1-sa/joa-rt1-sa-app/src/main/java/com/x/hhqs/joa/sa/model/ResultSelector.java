package com.x.hhqs.joa.sa.model;

import com.x.hhqs.joa.common.rt1fwk.exception.OssCommandException;
import com.x.hhqs.joa.sa.netconf.NetconfRequest;

public interface ResultSelector {
    void select(NetconfRequest.Selector selector, String rpcResponse) throws OssCommandException;
}
