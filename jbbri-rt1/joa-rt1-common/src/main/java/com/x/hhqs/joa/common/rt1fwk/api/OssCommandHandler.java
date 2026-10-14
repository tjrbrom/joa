package com.x.hhqs.joa.common.rt1fwk.api;

import com.x.hhqs.joa.common.api.UserInfo;
import com.x.hhqs.joa.common.rt1fwk.exception.OssCommandException;
import com.x.hhqs.joa.common.rt1fwk.pojo.OssCmdMeta;
import com.x.hhqs.joa.common.rt1fwk.pojo.OssCmdResult;
import com.x.hhqs.joa.rt1message.RT1InputMessage;

/**
 * the handler to realize the command;
 *
 * there could be different implementation for the same command depending on OLT type or release.
 */
public interface OssCommandHandler {
    OssCmdResult execute(OssCmdMeta devInfo, UserInfo userinfo) throws OssCommandException;
}
