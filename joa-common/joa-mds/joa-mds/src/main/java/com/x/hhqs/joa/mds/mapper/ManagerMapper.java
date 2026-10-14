package com.x.hhqs.joa.mds.mapper;

import java.util.List;
import java.util.stream.Collectors;
import com.x.hhqs.joa.mds.dto.ManagerDto;
import com.x.hhqs.joa.mds.model.Manager;
import com.x.hhqs.joa.mds.model.ManagerType;

public class ManagerMapper {

    public static List<ManagerDto> toDto(List<Manager> managers) {
        return managers.stream()
            .map(ManagerMapper::toDto)
            .collect(Collectors.toList());
    }

    public static ManagerDto toDto(Manager manager) {
        ManagerDto dto = new ManagerDto();

        dto.setName(manager.getName());
        dto.setType(manager.getType().name());
        dto.setIp(manager.getIp());
        dto.setConnectivityState(manager.getConnectivityState());
        dto.setUsername(manager.getUsername());
        dto.setProtocol(manager.getProtocol());
        dto.setPort(manager.getPort());
        dto.setLastSyncTime(manager.getLastSyncTime());
        dto.setLastSuccessSyncTime(manager.getLastSuccessSyncTime());

        if (manager.getType() == ManagerType.AMS) {
            dto.setActiveIp(manager.getActiveIp());
            dto.setNeGroup(manager.getNeGroup());
        } else if (manager.getType() == ManagerType.NAV) {
            dto.setHttpUrl(manager.getHttpUrl());
        }

        return dto;
    }
}
