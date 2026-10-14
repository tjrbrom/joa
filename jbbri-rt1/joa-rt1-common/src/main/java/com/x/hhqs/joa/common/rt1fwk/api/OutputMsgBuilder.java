package com.x.hhqs.joa.common.rt1fwk.api;

import com.x.hhqs.joa.common.rt1fwk.pojo.OssCmdResult;
import com.x.hhqs.joa.rt1message.RT1InputMessage;
import com.x.hhqs.joa.rt1message.RT1OutputMessage;

public interface OutputMsgBuilder {
    RT1OutputMessage buildResponseMessage(RT1InputMessage rt1InputMessage, OssCmdResult ossCmdResult);
}
