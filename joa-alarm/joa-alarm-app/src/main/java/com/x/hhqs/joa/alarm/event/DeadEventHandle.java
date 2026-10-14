package com.x.hhqs.joa.alarm.event;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DeadEventHandle implements IEventHandler {

    @Subscribe
    public void deadEventHandle(DeadEvent deadEvent){
        log.info("event dead");
    }

}
