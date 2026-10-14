package com.x.hhqs.joa.rt1agent.router;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.x.hhqs.joa.rt1agent.config.JbbriApplicationConfig;
import com.x.hhqs.joa.rt1agent.util.RT1AgentConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@ConditionalOnProperty(name = "joa.application.command-group.enabled", havingValue = "true")
public class CommandGroupRouter implements Router {

    @Autowired
    private JbbriApplicationConfig jbbriApplicationConfig;
    @Override
    public Optional<List<Instance>> route(List<Instance> instanceList, String command) {
        if (instanceList == null || instanceList.isEmpty()) {
            return Optional.empty();
        }
        Map<String, Set<String>> groups = jbbriApplicationConfig.getCommandGroup().getGroups();
        if (groups == null || groups.isEmpty()) {
            return Optional.of(instanceList);
        }
        Optional<List<Instance>> instances = getInstances(instanceList, command, groups);
        if (instances.isPresent()) return instances;
        return Optional.of(instanceList);
    }

    private Optional<List<Instance>> getInstances(List<Instance> instanceList, String command, Map<String, Set<String>> groups) {
        boolean config = false;
        List<Instance> instances = new ArrayList<>();
        for (Map.Entry<String, Set<String>> entry : groups.entrySet()) {
            String configGroup = entry.getKey();
            Set<String> configCodes = entry.getValue();
            if (configCodes.contains(command)) {
                config = true;
                for (Instance instance : instanceList) {
                    String commandGroup = instance.getMetadata().get(RT1AgentConstants.COMMAND_GROUP);
                    String commandCode = instance.getMetadata().get(RT1AgentConstants.COMMAND_CODE);
                    if (commandGroup.equals(configGroup) && commandCode.contains(command) && !instances.contains(instance)) {
                        instances.add(instance);
                    }
                }
            }
        }
        if (config) {
            return Optional.of(instances);
        }
        return Optional.empty();
    }
}
