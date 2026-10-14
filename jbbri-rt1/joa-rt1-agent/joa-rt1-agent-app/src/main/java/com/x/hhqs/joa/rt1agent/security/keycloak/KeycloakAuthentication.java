package com.x.hhqs.joa.rt1agent.security.keycloak;

import org.keycloak.representations.AccessToken;
import com.x.hhqs.joa.rt1agent.security.Authentication;
import com.x.hhqs.joa.rt1agent.security.User;

public class KeycloakAuthentication implements Authentication {

    private User user;

    private AccessToken accessToken;

    private boolean valid = false;

    private String errorMessage = null;

    public KeycloakAuthentication(User user) {
        super();
        this.user = user;
    }

    @Override
    public String getName() {
        return user.getUsername();
    }

    @Override
    public User getUserDetails() {
        return user;
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    public AccessToken getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(AccessToken accessToken) {
        this.accessToken = accessToken;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

}
