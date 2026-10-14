package com.x.hhqs.joa.rt1agent.command;

import com.x.hhqs.joa.common.message.JoaMessage;
import com.x.hhqs.joa.rt1agent.command.EaddCode.FailResult;

public class FailResultException extends Exception {

    private static final long serialVersionUID = 1L;

    private FailResult failResult;
    
    private Object[] args;

    public FailResultException(FailResult failResult) {
        this.failResult = failResult;
    }
    
    public FailResultException(FailResult failResult, Object[] args) {
        this.failResult = failResult;
        this.args = args;
    }

    public FailResult getFailResult() {
        return failResult;
    }
    
    @Override
    public String getMessage() {
        return JoaMessage.instance().getMessage(failResult.eadd, args);
    }

    public Object[] getArgs() {
        return args;
    }

}

