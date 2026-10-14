package com.x.hhqs.joa.sa.api;

/**
 * The types of requests currently supported by southbound can be increased in the future.
 */
public enum RequestType {
    NETCONF("netconf"),
    RESTCONF("restconf");

    private final String type;

    RequestType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }
}
