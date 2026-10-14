package com.x.hhqs.joa.mds.mapper;

import java.util.List;
import java.util.stream.Collectors;
import com.x.hhqs.joa.mds.dto.AmsDeviceDto;
import com.x.hhqs.joa.mds.dto.DeviceDto;
import com.x.hhqs.joa.mds.dto.NavDeviceDto;
import com.x.hhqs.joa.mds.model.Device;
import com.x.hhqs.joa.mds.model.ManagerType;

public class DeviceMapper {

    public static List<DeviceDto> toDto(List<Device> devices) {
        return devices.stream()
            .map(DeviceMapper::toDto)
            .collect(Collectors.toList());
    }

    public static DeviceDto toDto(Device device) {
        if (device.getManagerType() == ManagerType.AMS) {
            AmsDeviceDto dto = new AmsDeviceDto();
            fillBaseDeviceInfo(dto, device);

            dto.setId(device.getId());
            dto.setSubrackModel(device.getSubrackModel());
            dto.setSubType(device.getSubType());
            dto.setProductAndRelease(device.getProductAndRelease());

            return dto;
        }

        if (device.getManagerType() == ManagerType.NAV) {
            NavDeviceDto dto = new NavDeviceDto();
            fillBaseDeviceInfo(dto, device);

            dto.setDeviceVersion(device.getDeviceVersion());
            dto.setHardwareType(device.getHardwareType());

            return dto;
        }

        throw new IllegalArgumentException("Invalid device managerType:" + device.getManagerType());
    }

    public static void fillBaseDeviceInfo(DeviceDto dto, Device device) {
        dto.setName(device.getName());
        dto.setManagerName(device.getManagerName());
        dto.setManagerType(device.getManagerType().name());
        dto.setFamilyTypeRelease(device.getFamilyTypeRelease());
        dto.setIpAddress(device.getIpAddress());
    }

}
