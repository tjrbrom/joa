package com.x.hhqs.joa.sa.controller;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.x.hhqs.joa.common.api.RT1CommandRequest;
import com.x.hhqs.joa.common.api.RT1CommandResponse;
import com.x.hhqs.joa.common.rt1fwk.api.OssCommandService;

@RestController
public class SaController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SaController.class);

    @Autowired
    private OssCommandService ossCommandService;
    
    @PostMapping("/rt1request")
    public RT1CommandResponse rt1CommandRequest(@RequestBody RT1CommandRequest commandRequest) {
        String response = ossCommandService.execute(commandRequest.getCommand(), commandRequest.getUserInfo());
        LOGGER.info(commandRequest.getCommand() + " " + response);
        
        RT1CommandResponse commandResponse = buildRT1CommandResponse(response, commandRequest);
        return commandResponse;
    }
    
    protected RT1CommandResponse buildRT1CommandResponse(String response, RT1CommandRequest commandRequest) {
        RT1CommandResponse commandResponse = new RT1CommandResponse(commandRequest.getRequestId(), List.of(response));
        commandResponse.setSessionId(commandRequest.getUserInfo().getSessionId());

        return commandResponse;
    }
}
