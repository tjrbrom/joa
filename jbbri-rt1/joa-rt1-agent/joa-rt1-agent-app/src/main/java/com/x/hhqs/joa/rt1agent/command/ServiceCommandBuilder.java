package com.x.hhqs.joa.rt1agent.command;

import java.util.Optional;

import com.x.hhqs.joa.rt1agent.config.JbbriApplicationConfig;
import com.x.hhqs.joa.rt1agent.service.RT1CommandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ServiceCommandBuilder {

	@Autowired
	private RT1CommandService commandService;

	@Autowired
	private JbbriApplicationConfig jbbriApplicationConfig;

	public ServiceCommand create(String commandCode) {
		ServiceCommand command = new ServiceCommand(commandService);
		command.setCommandCode(commandCode);

		Optional<String> group = getCommandCodeGroup(commandCode);
		if (group.isPresent()) {
			command.setCommandGroup(group.get());
		}

		return command;
	}

	public ServiceCommand create(String commandCode, String commandGroup) {
		ServiceCommand command = new ServiceCommand(commandService);
		command.setCommandCode(commandCode);
		command.setCommandGroup(commandGroup);

		return command;
	}

	private Optional<String> getCommandCodeGroup(String commandCode) {
		if (!jbbriApplicationConfig.getCommandGroup().isEnabled()) {
			return Optional.empty();
		}

		return jbbriApplicationConfig.getCommandGroup().getGroups().entrySet()
				.stream()
				.filter(entry -> entry.getValue().contains(commandCode))
				.map(entry -> entry.getKey())
				.findFirst();
	}

}
