package com.x.hhqs.joa.rt1agent.command;

import com.x.hhqs.joa.common.message.JoaMessage;
import com.x.hhqs.joa.common.rt1fwk.api.OutputMsgBuilder;
import com.x.hhqs.joa.common.rt1fwk.pojo.OssCmdResult;
import com.x.hhqs.joa.common.util.CommonConstants;
import com.x.hhqs.joa.rt1agent.command.EaddCode.FailResult;
import com.x.hhqs.joa.rt1message.RT1InputMessage;
import com.x.hhqs.joa.rt1message.RT1OutputMessage;

public class RT1AgentResponseBuilder {

    public OssCmdResult success = new OssCmdResult(true);

    private OutputMsgBuilder outputMsgBuilder;

    private JoaMessage i18n;

    public RT1AgentResponseBuilder(OutputMsgBuilder outputMsgBuilder, JoaMessage joaMessage) {
        this.outputMsgBuilder = outputMsgBuilder;
        this.i18n = joaMessage;
    }

    public RT1OutputMessage response(FailResult result) {
        return response(null, result);
    }

    public RT1OutputMessage response(RT1InputMessage inputMessage, FailResult result) {
        return outputMsgBuilder.buildResponseMessage(inputMessage, result.toOssCmdResult(i18n));
    }
    
    public RT1OutputMessage response(RT1InputMessage inputMessage, FailResult result, Object[] args) {
        return outputMsgBuilder.buildResponseMessage(inputMessage, result.toOssCmdResult(i18n, args));
    }
    
    public RT1OutputMessage response(RT1InputMessage inputMessage, String errorCode, String exception) {
        OssCmdResult result = new OssCmdResult(false, errorCode, exception);
        return outputMsgBuilder.buildResponseMessage(inputMessage, result);
    }

    public RT1OutputMessage exceptionResponse(RT1InputMessage inputMessage, String exception) {
        OssCmdResult result = new OssCmdResult(false, CommonConstants.EEEH, exception);
        return outputMsgBuilder.buildResponseMessage(inputMessage, result);
    }

    public RT1OutputMessage success(RT1InputMessage inputMessage) {
        return outputMsgBuilder.buildResponseMessage(inputMessage, success);
    }

}
