package com.x.hhqs.joa.rt1agent.router;

import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;
import java.util.Optional;

public interface Router {
    Optional<List<Instance>> route(List<Instance> instanceList, String commandCode);
}
