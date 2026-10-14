package com.x.hhqs.joa.rt1agent.security.dev;

import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;
import com.x.hhqs.joa.rt1agent.security.Authentication;
import com.x.hhqs.joa.rt1agent.security.PermissionCheckResult;
import com.x.hhqs.joa.rt1agent.security.SecurityService;
import com.x.hhqs.joa.rt1agent.security.User;

@Service
@ConditionalOnProperty(prefix = "joa.rt1.security", name = "mode", havingValue = "dev")
@Import(DevUserProperties.class)
public class DevSercurityService implements SecurityService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DevSercurityService.class);

    @Autowired
    private DevUserProperties userProperties;

    @PostConstruct
    public void init() {
        LOGGER.info("security.mode: dev");
    }

    @Override
    public Authentication login(String username, String password) {
        User user = new User(username);

        if (username.equalsIgnoreCase(userProperties.getName())) {
            DevAuthentication authentication = new DevAuthentication(user, true);
            user.setPapGroups(userProperties.getPapGroups());
            user.setPaps(userProperties.getPaps());

            return authentication;
        }

        LOGGER.error("Wrong username or password.");
        return new DevAuthentication(user, false);
    }

    @Override
    public void logout(Authentication authentication) {
        // empty
    }

    @Override
    public boolean hasAuthorization(Authentication authentication, String commandCode) {
        return true;
    }

    @Override
    public PermissionCheckResult checkSecondaryAuthPermission(String username, String password) {
        return PermissionCheckResult.allow();
    }

}
