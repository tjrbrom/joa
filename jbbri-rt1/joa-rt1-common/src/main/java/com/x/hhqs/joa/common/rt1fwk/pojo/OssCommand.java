package com.x.hhqs.joa.common.rt1fwk.pojo;


import com.x.hhqs.joa.common.rt1fwk.api.OssCommandHandler;
import com.x.hhqs.joa.common.rt1fwk.api.OssCommandParser;
import com.x.hhqs.joa.common.rt1fwk.api.OutputMsgBuilder;

import java.util.Map;

public class OssCommand {
    private String cmdName;
    private Validation validation;
    private OssCommandParser parser;
    private Map<DeviceInfo, OssCommandHandler> handlerMap;
    private OutputMsgBuilder builder;


    public String getCmdName() {
        return cmdName;
    }

    public void setCmdName(String cmdName) {
        this.cmdName = cmdName;
    }

    public Validation getValidation() {
        return validation;
    }

    public void setValidation(Validation validation) {
        this.validation = validation;
    }

    public OssCommandParser getParser() {
        return parser;
    }

    public void setParser(OssCommandParser parser) {
        this.parser = parser;
    }

    public Map<DeviceInfo, OssCommandHandler> getHandlerMap() {
        return handlerMap;
    }

    public void setHandlerMap(Map<DeviceInfo, OssCommandHandler> handlerMap) {
        this.handlerMap = handlerMap;
    }

    public OutputMsgBuilder getBuilder() {
        return builder;
    }

    public void setBuilder(OutputMsgBuilder builder) {
        this.builder = builder;
    }

}
