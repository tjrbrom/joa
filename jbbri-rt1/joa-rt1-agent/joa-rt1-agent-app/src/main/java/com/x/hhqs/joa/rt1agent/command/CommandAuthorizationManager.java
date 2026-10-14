package com.x.hhqs.joa.rt1agent.command;

import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.x.hhqs.joa.rt1agent.config.AipConfig;
import com.x.hhqs.joa.rt1agent.config.VaultAuthConfiguration;
import com.x.hhqs.joa.rt1message.RT1InputMessage;

@Service
@RefreshScope
public class CommandAuthorizationManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandAuthorizationManager.class);

    @Autowired
    private VaultAuthConfiguration vaultAuthCfg;

    @Autowired
    private AipConfig aipConfig;

    @Autowired
    private RestTemplate restTemplate;

    public boolean isAuthorizationRequired(CommandAuthorizationContext cmdAuthContext, RT1InputMessage msg)
        throws FailResultException {
        if (!vaultAuthCfg.isEnabled()) {
            cmdAuthContext.reset();
            return false;
        }

        try {
            if (!isSecondaryAuthorizationCommand(msg)) {
                cmdAuthContext.reset();
                return false;
            }
        } catch (Exception e) {
            cmdAuthContext.reset();
            LOGGER.error(e.getMessage(), e);
            throw new FailResultException(EaddCode.CHECK_AIP_COMMANDS_EXCEPTION);
        }

        if (cmdAuthContext.isAuthorized()) {
            if (cmdAuthContext.isAuthorizedCommand(msg.toString())) {
                cmdAuthContext.reset();
                return false;
            }
            cmdAuthContext.reset();
            throw new FailResultException(EaddCode.AUTHORIZED_COMMAND_MISMATCH);
        }

        cmdAuthContext.pendingAuthCommand(msg.toString());
        return true;
    }

    boolean isSecondaryAuthorizationCommand(RT1InputMessage msg) throws Exception {
        String url = getAipUrl();
        LOGGER.info("aip url: {}", url);

        ResponseEntity<Set<String>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<Set<String>>() {});

        LOGGER.info("command codes: {}" + response.getBody());
        Set<String> commands = response.getBody();
        if (CollectionUtils.isNotEmpty(commands)) {
            for (String code : commands) {
                if (code.equalsIgnoreCase(msg.getCommandCode().toUpperCase())) {
                    return true;
                }
            }
        }

        return false;
    }

    String getAipUrl() {
        if (aipConfig.getBaseUrl().startsWith("http://")) {
            return aipConfig.getBaseUrl() + "/rest/rt1/commands";
        }
        return "http://" + aipConfig.getBaseUrl() + "/rest/rt1/commands";
    }

}
