package com.x.hhqs.joa.rt1agent.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;

public abstract class InternalCommand implements Command {
	public static final String GROUP = "rt1agent";

	@Autowired
	protected CommandRegistry commandRegistry;

	public InternalCommand() {
		super();
	}

	@Override
	public String getCommandGroup() {
		return GROUP;
	}

	@PostConstruct
	public void register() {
		commandRegistry.register(getCommandCode(), this);
	}

	@PreDestroy
	public void unregister() {
		commandRegistry.unRegister(getCommandCode());
	}

	@Inherited
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Anonymous {
		// no value
	}
	
}
