package com.x.hhqs.joa.alarm.service.impl;


import com.x.hhqs.joa.alarm.api.CommandHandler;
import com.x.hhqs.joa.alarm.api.CommandHandlerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class CommandHandlerRegistryImpl implements CommandHandlerRegistry {

	private final Map<String, CommandHandler> handlers = new ConcurrentHashMap<>();

	@Override
	public void register(String commandCode, CommandHandler handler) {
		log.info("Register CommandCode [" + commandCode + "]");
		handlers.put(commandCode, handler);
	}

	@Override
	public void unRegister(String commandCode) {
		log.info("Unregister CommandCode [" + commandCode + "]");
		handlers.remove(commandCode);
	}

	@Override
	public CommandHandler getCommandHandler(String commandCode) {
		return handlers.get(commandCode);
	}

}
