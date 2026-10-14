package com.x.hhqs.joa.rt1agent.security;

import java.util.Set;

public class User implements UserDetails {

    private static final long serialVersionUID = 1L;

    private String password;

    private final String username;

    private Set<String> authorities;

    private Set<String> papGroups;

    private Set<String> paps;

    public User(String username) {
        this.username = username;
    }

    @Override
    public Set<String> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public Set<String> getPapGroups() {
        return this.papGroups;
    }

    @Override
    public Set<String> getPaps() {
        return this.paps;
    }

    public void setPapGroups(Set<String> papGroups) {
        this.papGroups = papGroups;
    }

    public void setPaps(Set<String> paps) {
        this.paps = paps;
    }

    public void setAuthorities(Set<String> authorities) {
        this.authorities = authorities;
    }

}
