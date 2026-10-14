package com.x.hhqs.joa.alarm.api;

public interface CommandHandlerRegistry {

    void register(String commandCode, CommandHandler handler);

    void unRegister(String commandCode);

    CommandHandler getCommandHandler(String commandCode);

}
