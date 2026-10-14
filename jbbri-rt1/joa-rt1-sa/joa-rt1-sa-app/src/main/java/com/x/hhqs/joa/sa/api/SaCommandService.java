package com.x.hhqs.joa.sa.api;

/**
 * This is the service command interface of the SA module.
 */
public interface SaCommandService {
    boolean supportCommand(String commandCode);
    String execute(String inputMessage);
}
