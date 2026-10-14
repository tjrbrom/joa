package com.x.hhqs.joa.rt1agent.server;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import com.x.hhqs.joa.rt1agent.command.CommandAuthorizationContext;
import com.x.hhqs.joa.rt1agent.security.Authentication;

enum State {
    LOGIN, LOGOUT, UNKNOWN
}


public class SessionInfo {
    private String sessionId;

    private long creationTime;

    private State state = State.UNKNOWN;

    private Authentication authentication;
    
    private AtomicInteger reqCounter;
    
    private CommandAuthorizationContext commandAuthorizationContext;

    private SessionInfo(Authentication authentication, State state) {
        this.authentication = authentication;
        this.state = state;
        this.sessionId = UUID.randomUUID().toString();
        this.creationTime = System.currentTimeMillis();
        this.reqCounter = new AtomicInteger(1);
        this.commandAuthorizationContext = new CommandAuthorizationContext();
    }

    public boolean isLogin() {
        return state.equals(State.LOGIN);
    }

    public String getSessionId() {
        return sessionId;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public static SessionInfo newLoginSessionInfo(Authentication authentication) {
        return new SessionInfo(authentication, State.LOGIN);
    }

    public void logout() {
        this.state = State.LOGOUT;
    }

    public Authentication getAuthentication() {
        return authentication;
    }

    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }

    public long getCreationTime() {
        return creationTime;
    }
    
    public String getNextRequestId() {
        int id = reqCounter.getAndIncrement();
        return "req-" + id;
    }

    public CommandAuthorizationContext getCommandAuthorizationContext() {
        return commandAuthorizationContext;
    }
    
    public String getUsername() {
        return authentication.getName();
    }

}
