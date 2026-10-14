package com.x.hhqs.joa.rt1agent.command;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommandRegistry {
	private Map<String, Command> internalCommands = new ConcurrentHashMap<>();

	@Autowired
	private ServiceCommandBuilder serviceCommandBuilder;

	public void register(String commandCode, Command command) {
		internalCommands.put(commandCode, command);
	}

	public void unRegister(String commandCode) {
		internalCommands.remove(commandCode);
	}

	public Command getCommand(String commandCode) {
		if (isInternalCommand(commandCode)) {
			return internalCommands.get(commandCode);
		}

		return serviceCommandBuilder.create(commandCode);
	}

	public boolean isInternalCommand(String commandCode) {
		return internalCommands.containsKey(commandCode);
	}

}
