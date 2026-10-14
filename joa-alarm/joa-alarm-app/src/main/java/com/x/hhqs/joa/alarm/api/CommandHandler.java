package com.x.hhqs.joa.alarm.api;

import io.netty.channel.Channel;

public interface CommandHandler {

	String getCommandCode();

	void execute(Channel ch, Command cmd);

}
