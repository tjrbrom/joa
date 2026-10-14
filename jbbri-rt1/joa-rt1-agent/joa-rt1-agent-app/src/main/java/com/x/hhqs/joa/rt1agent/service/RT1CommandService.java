package com.x.hhqs.joa.rt1agent.service;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.x.hhqs.joa.common.api.RT1CommandRequest;
import com.x.hhqs.joa.common.api.RT1CommandResponse;
import com.x.hhqs.joa.rt1agent.command.FailResultException;

public interface RT1CommandService {

    @PostMapping("/rt1request")
    RT1CommandResponse invoke(String commandCode, @RequestBody RT1CommandRequest commandRequest) throws FailResultException;

}
