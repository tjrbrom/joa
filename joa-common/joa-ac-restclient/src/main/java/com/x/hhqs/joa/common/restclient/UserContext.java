package com.x.hhqs.joa.common.restclient;

public class UserContext extends Pair<String, String> {
    public UserContext(String username, String sessionId) {
        super(username, sessionId);
    }

    public String getUsername() {
        return getFirst();
    }

    public String getSessionId() {
        return getSecond();
    }

    @Override
    public String toString() {
        return "UserContext{" +
                "m_username='" + getUsername() + '\'' +
                ", m_sessionId='" + getSessionId() + '\'' +
                '}';
    }

    public UserContext copy() {
        return new UserContext(getUsername(), getSessionId());
    }
}
