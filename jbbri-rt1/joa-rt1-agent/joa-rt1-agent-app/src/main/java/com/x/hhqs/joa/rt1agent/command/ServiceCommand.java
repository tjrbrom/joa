package com.x.hhqs.joa.rt1agent.command;

import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.x.hhqs.joa.common.api.RT1CommandRequest;
import com.x.hhqs.joa.common.api.RT1CommandResponse;
import com.x.hhqs.joa.common.api.UserInfo;
import com.x.hhqs.joa.rt1agent.security.UserDetails;
import com.x.hhqs.joa.rt1agent.server.ContextUtil;
import com.x.hhqs.joa.rt1agent.server.SessionInfo;
import com.x.hhqs.joa.rt1agent.service.RT1CommandService;
import com.x.hhqs.joa.rt1message.RT1InputMessage;
import io.netty.channel.ChannelHandlerContext;

public class ServiceCommand implements Command {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCommand.class);

    private RT1CommandService commandService;

    private String commandCode;

    private String commandGroup;

    public ServiceCommand(RT1CommandService commandService) {
        super();
        this.commandService = commandService;
    }

    @Override
    public Object execute(ChannelHandlerContext ctx, RT1InputMessage msg) throws Exception {
        SessionInfo sessionInfo = ContextUtil.getSession(ctx);
        UserInfo userInfo = buildUserInfo(sessionInfo);

        RT1CommandRequest commandRequest =
            new RT1CommandRequest(msg.toString(), userInfo, sessionInfo.getNextRequestId());

        LOGGER.info("#[{} {} {}] execute ServiceCommand: {}", userInfo.getUsername(),
            sessionInfo.getSessionId(), commandRequest.getRequestId(), msg.toString());

        try {
            RT1CommandResponse commandResponse = commandService.invoke(msg.getCommandCode(), commandRequest);
            return commandResponse.getResponses();
        } catch (FailResultException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new FailResultException(EaddCode.INVOKE_COMMAND_SERVICE_FAIL);
        }
    }

    protected UserInfo buildUserInfo(SessionInfo sessionInfo) {
        UserDetails user = sessionInfo.getAuthentication().getUserDetails();
        UserInfo userInfo = new UserInfo(user.getUsername(), sessionInfo.getSessionId());
        if (user.getPaps() == null) {
            userInfo.setPaps(Set.of());
        } else {
            userInfo.setPaps(user.getPaps());
        }

        return userInfo;
    }

    @Override
    public String getCommandGroup() {
        return commandGroup;
    }

    @Override
    public String getCommandCode() {
        return commandCode;
    }

    public void setCommandCode(String commandCode) {
        this.commandCode = commandCode;
    }

    public void setCommandGroup(String commandGroup) {
        this.commandGroup = commandGroup;
    }

}
