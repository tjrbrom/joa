package com.x.hhqs.joa.rt1agent;

import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

@Component
public class AppInfo {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppInfo.class);
    
    @Autowired(required=false)
    private BuildProperties buildProperties;
    
    @PostConstruct
    public void init() {
        if(buildProperties != null) {
            LOGGER.info("{}, {}", buildProperties.getVersion(), buildProperties.getTime());
        }
    }

}
