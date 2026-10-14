package com.x.hhqs.joa.sa.netconf;

/**
 * The RestClient currently supports two types of request bodies, which may be added in the future.
 *
 */
public enum ContentType {
    APPLICATION_XML("application/xml"),
    APPLICATION_JSON("application/json");

    private final String header;

    ContentType(String header) {
        this.header = header;
    }

    public String getHeader() {
        return this.header;
    }
}
