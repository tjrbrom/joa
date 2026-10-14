package com.x.hhqs.joa.rt1agent.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;

public class AsyncTaskManager {

	@Autowired
	private TaskExecutor taskExecutor;

	public void execute(Runnable task) {
		taskExecutor.execute(task);
	}
}
