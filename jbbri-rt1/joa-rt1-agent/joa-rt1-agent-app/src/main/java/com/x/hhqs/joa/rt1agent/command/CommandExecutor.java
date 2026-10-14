package com.x.hhqs.joa.rt1agent.command;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import com.x.hhqs.joa.rt1agent.command.InternalCommand.Anonymous;
import com.x.hhqs.joa.rt1agent.forwarding.ponnbi.PonnbiProxy;
import com.x.hhqs.joa.rt1agent.security.SecurityService;
import com.x.hhqs.joa.rt1agent.server.ContextUtil;
import com.x.hhqs.joa.rt1agent.server.SessionInfo;
import com.x.hhqs.joa.rt1agent.service.JoaMdsService;
import com.x.hhqs.joa.rt1agent.util.Rt1MessageUtil;
import com.x.hhqs.joa.rt1message.RT1InputMessage;
import io.netty.channel.ChannelHandlerContext;

@Component
public class CommandExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandExecutor.class);

    @Autowired
    private CommandRegistry commandRegistry;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private RT1AgentResponseBuilder responseBuilder;

    @Autowired
    private CommandAuthorizationManager cmdAuthManager;

    @Autowired
    private PonnbiProxy ponnbiProxy;

    @Autowired
    private JoaMdsService mdsService;

    public Object execute(ChannelHandlerContext ctx, RT1InputMessage msg) {
        try {
            String commandCode = msg.getCommandCode();
            if (!StringUtils.hasText(commandCode)) {
                return responseBuilder.response(msg, EaddCode.INVALID_COMMAND_CODE);
            }
            commandCode = commandCode.toUpperCase();
            Command command = commandRegistry.getCommand(commandCode);

            // 1. execute if command can be executed without login
            // InternalCommand with @Anonymous annotation can be executed without login
            if (isAnonymous(command)) {
                return command.execute(ctx, msg);
            }

            // 2.Check if login
            SessionInfo sessionInfo = ContextUtil.getSession(ctx);
            if (clientNotLogin(sessionInfo)) {
                return responseBuilder.response(msg, EaddCode.NOT_LOGIN);
            }

            // 3. InternalCommand is handled by rt1agent
            // can be executed by all users
            if (isInternalCommand(command)) {
                return command.execute(ctx, msg);
            }

            // 4.check if client has permission to execute command
            if (!hasPermission(sessionInfo, commandCode)) {
                LOGGER.warn("No permission for commandCode: {}", commandCode);
                return responseBuilder.response(msg, EaddCode.NO_PERMISSION);
            }

            // 5.secondary authorization
            if (cmdAuthManager.isAuthorizationRequired(sessionInfo.getCommandAuthorizationContext(), msg)) {
                return responseBuilder.response(msg, EaddCode.NEED_SECONDARY_AUTHORIZATION);
            }

            // 6. check if send to PONNBI
            if (ponnbiProxy.isEnable() && isPonnbiCommand(msg)) {
                return ponnbiProxy.sendMessage(msg, sessionInfo.getSessionId());
            }

            // 7.execute command
            return command.execute(ctx, msg);
        } catch (FailResultException e) {
            LOGGER.error(e.getMessage(), e);
            if (e.getArgs() != null) {
                return responseBuilder.response(msg, e.getFailResult(), e.getArgs());
            } else {
                return responseBuilder.response(msg, e.getFailResult());
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return responseBuilder.exceptionResponse(msg, e.getMessage());
        }
    }

    boolean isPonnbiCommand(RT1InputMessage msg) throws FailResultException {
        Map<String, String> aid = Rt1MessageUtil.parseAidToMap(msg.getAid());
        String oltId = aid.get("OLTID");
        if (!StringUtils.hasText(oltId)) {
            return false;
        }

        return mdsService.isAmsDevice(oltId);
    }

    private boolean isLogin(SessionInfo sessionInfo) {
        if (sessionInfo == null) {
            return false;
        }

        return sessionInfo.isLogin();
    }

    private boolean hasPermission(SessionInfo sessionInfo, String commandCode) {
        return securityService.hasAuthorization(sessionInfo.getAuthentication(), commandCode);
    }

    private boolean clientNotLogin(SessionInfo sessionInfo) {
        return !isLogin(sessionInfo);
    }

    public Boolean isAnonymous(Command command) {
        if (command == null) {
            return false;
        }
        Class<?> clazz = command.getClass();
        return clazz.isAnnotationPresent(Anonymous.class);
    }

    public Boolean isInternalCommand(Command command) {
        return command instanceof InternalCommand;
    }

}
