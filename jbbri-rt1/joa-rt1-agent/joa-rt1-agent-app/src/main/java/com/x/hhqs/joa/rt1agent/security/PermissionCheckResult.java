package com.x.hhqs.joa.rt1agent.security;

public class PermissionCheckResult {

    private final boolean allowed;
    private final String errorMessage;

    private PermissionCheckResult(boolean allowed, String errorMessage) {
        this.allowed = allowed;
        this.errorMessage = errorMessage;
    }

    public static PermissionCheckResult allow() {
        return new PermissionCheckResult(true, null);
    }

    public static PermissionCheckResult deny() {
        return new PermissionCheckResult(false, null);
    }

    public static PermissionCheckResult error(String message) {
        return new PermissionCheckResult(false, message);
    }

    public boolean isAllowed() {
        return allowed;
    }
    
    public boolean hasErrorMessage() {
        return errorMessage != null;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
