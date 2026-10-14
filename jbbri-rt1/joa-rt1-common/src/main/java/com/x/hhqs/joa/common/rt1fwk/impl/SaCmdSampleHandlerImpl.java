package com.x.hhqs.joa.common.rt1fwk.impl;

import com.x.hhqs.joa.common.api.UserInfo;
import com.x.hhqs.joa.common.rt1fwk.exception.OssCommandException;
import com.x.hhqs.joa.common.rt1fwk.pojo.OssCmdMeta;
import com.x.hhqs.joa.common.rt1fwk.pojo.OssCmdResult;
import com.x.hhqs.joa.common.rt1fwk.api.OssCommandHandler;
import org.springframework.stereotype.Component;

@Component("LS-FX.SAHandler")
public class SaCmdSampleHandlerImpl implements OssCommandHandler {

    @Override
    public OssCmdResult execute(OssCmdMeta devInfo, UserInfo userinfo) throws OssCommandException {
        OssCmdResult ossCmdResult = new OssCmdResult(false);
        ossCmdResult.setErrorCode("TEST");
        return ossCmdResult;
    }
}
