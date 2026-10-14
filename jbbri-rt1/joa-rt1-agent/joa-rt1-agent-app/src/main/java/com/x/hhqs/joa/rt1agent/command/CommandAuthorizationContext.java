package com.x.hhqs.joa.rt1agent.command;

public class CommandAuthorizationContext {

    private String pendingAuthCommand;

    private String authorizedCommand;

    public void reset() {
        pendingAuthCommand = null;
        authorizedCommand = null;
    }

    public void authorized() {
        authorizedCommand = pendingAuthCommand;
        pendingAuthCommand = null;
    }

    public boolean isAuthorized() {
        return authorizedCommand != null;
    }
    
    public boolean isAuthorizedCommand(String command) {
        return command != null && command.equals(authorizedCommand);
    }
    
    public boolean isPendingAuth() {
        return pendingAuthCommand != null;
    }

    public void pendingAuthCommand(String pendingAuthCommand) {
        reset();
        this.pendingAuthCommand = pendingAuthCommand;
    }

    public String getPendingAuthCommand() {
        return pendingAuthCommand;
    }

    public String getAuthorizedCommand() {
        return authorizedCommand;
    }

}
