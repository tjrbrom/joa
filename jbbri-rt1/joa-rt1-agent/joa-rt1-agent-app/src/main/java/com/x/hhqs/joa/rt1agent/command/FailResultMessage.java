package com.x.hhqs.joa.rt1agent.command;

import com.x.hhqs.joa.rt1agent.command.EaddCode.FailResult;
import com.x.hhqs.joa.rt1message.RT1InputMessage;

public class FailResultMessage {

    private final RT1InputMessage rt1Message;

    private final FailResult failResult;

    public FailResultMessage(RT1InputMessage rt1Message, FailResult failResult) {
        this.rt1Message = rt1Message;
        this.failResult = failResult;
    }

    public RT1InputMessage getRt1Message() {
        return rt1Message;
    }

    public FailResult getFailResult() {
        return failResult;
    }

}
