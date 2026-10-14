package com.x.hhqs.joa.rt1agent.forwarding.ponnbi;

import com.x.hhqs.joa.rt1message.RT1ResponseMessage;

public class RT1ResponseWrapper {

    private final String ctag;
    private final String rawResponse;
    private final RT1ResponseMessage rt1Message;

    public RT1ResponseWrapper(String rawResponse, String ctag, RT1ResponseMessage rt1Message) {
        this.rawResponse = rawResponse;
        this.ctag = ctag;
        this.rt1Message = rt1Message;
    }

    public String getCtag() {
        return ctag;
    }

    public String getRawResponse() {
        return rawResponse;
    }

    public RT1ResponseMessage getRt1Message() {
        return rt1Message;
    }

}
