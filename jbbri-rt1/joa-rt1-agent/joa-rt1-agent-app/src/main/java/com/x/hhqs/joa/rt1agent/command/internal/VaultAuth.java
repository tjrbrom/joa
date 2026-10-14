package com.x.hhqs.joa.rt1agent.command.internal;

import java.util.Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import com.x.hhqs.joa.rt1agent.command.CommandAuthorizationContext;
import com.x.hhqs.joa.rt1agent.command.EaddCode;
import com.x.hhqs.joa.rt1agent.command.InternalCommand;
import com.x.hhqs.joa.rt1agent.command.RT1AgentResponseBuilder;
import com.x.hhqs.joa.rt1agent.config.VaultAuthConfiguration;
import com.x.hhqs.joa.rt1agent.security.PermissionCheckResult;
import com.x.hhqs.joa.rt1agent.security.SecurityService;
import com.x.hhqs.joa.rt1agent.server.ContextUtil;
import com.x.hhqs.joa.rt1agent.server.SessionInfo;
import com.x.hhqs.joa.rt1agent.util.Rt1MessageUtil;
import com.x.hhqs.joa.rt1message.RT1InputMessage;
import io.netty.channel.ChannelHandlerContext;

@Component
public class VaultAuth extends InternalCommand {
    private String commandCode = "VAULT-AUTH";

    @Autowired
    private SecurityService securityService;

    @Autowired
    private RT1AgentResponseBuilder responseBuilder;

    @Autowired
    private VaultAuthConfiguration vaultAuthCfg;

    // e.g. VAULT-AUTH:::ctag::UN=manager,PWD=123456;
    @Override
    public Object execute(ChannelHandlerContext ctx, RT1InputMessage rt1msg) throws Exception {
        SessionInfo sessionInfo = ContextUtil.getSession(ctx);
        CommandAuthorizationContext cmdAuthContext = sessionInfo.getCommandAuthorizationContext();
        
        if (!vaultAuthCfg.isEnabled()) {
            cmdAuthContext.reset();
            return responseBuilder.response(rt1msg, EaddCode.INVALID_COMMAND_CODE);
        }

        if (!cmdAuthContext.isPendingAuth()) {
            cmdAuthContext.reset();
            return responseBuilder.response(rt1msg, EaddCode.NO_PENDING_AUTH_COMMAND);
        }

        Properties params = Rt1MessageUtil.generateParameters(rt1msg);
        String username = params.getProperty("UN");
        String password = params.getProperty("PWD");
        
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            cmdAuthContext.reset();
            return responseBuilder.response(rt1msg, EaddCode.USERNAME_OR_PASSWORD_MISSING);
        }

        if (sessionInfo.getUsername().equalsIgnoreCase(username)) {
            cmdAuthContext.reset();
            return responseBuilder.response(rt1msg, EaddCode.REQUIRE_ADDITIONAL_USER);
        }

        PermissionCheckResult permissionCheck = securityService.checkSecondaryAuthPermission(username, password);
        if (!permissionCheck.isAllowed()) {
            cmdAuthContext.reset();
            if (permissionCheck.hasErrorMessage()) {
                return responseBuilder.exceptionResponse(rt1msg, permissionCheck.getErrorMessage());
            } else {
                return responseBuilder.response(rt1msg, EaddCode.NO_PRIVILEGE_FOR_VAULT_AUTH);
            }
        }

        cmdAuthContext.authorized();
        return responseBuilder.success(rt1msg);
    }


    @Override
    public String getCommandCode() {
        return commandCode;
    }

}
