package com.x.hhqs.joa.sa.model;

import com.x.hhqs.joa.sa.netconf.NetconfRequest;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("directAssignment")
public class DirectAssignmentProcessor implements ModelDataProcessor {
    @Override
    public void process(NetconfRequest.Data data, Map<String, Object> model) {
        model.put(data.getName(), data.getValue());
        if (Boolean.TRUE.equals(data.getToContext())) {
            Map<String, String> contextData = ModelDataContainer.getSharedData().getContext();
            contextData.put(data.getName(), data.getValue());
        }
    }
}
