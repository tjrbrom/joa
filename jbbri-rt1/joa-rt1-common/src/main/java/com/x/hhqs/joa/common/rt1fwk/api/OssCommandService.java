package com.x.hhqs.joa.common.rt1fwk.api;

import com.x.hhqs.joa.common.api.UserInfo;
import org.springframework.scheduling.concurrent.ScheduledExecutorTask;

import java.util.Set;

public interface OssCommandService {

    Set<String> getCommandCodes();

    String execute(String inputMessage, UserInfo userInfo);
}
