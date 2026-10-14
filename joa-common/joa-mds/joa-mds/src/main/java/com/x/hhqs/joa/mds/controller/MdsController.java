package com.x.hhqs.joa.mds.controller;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.x.hhqs.joa.mds.mapper.DeviceMapper;
import com.x.hhqs.joa.mds.mapper.ManagerMapper;
import com.x.hhqs.joa.mds.model.Device;
import com.x.hhqs.joa.mds.model.Manager;
import com.x.hhqs.joa.mds.sync.MdsContextService;

@RestController
@RequestMapping("/api/v1/mds")
public class MdsController {
    @Autowired
    private MdsContextService mdsContextService;

    @GetMapping("/managers")
    public ResponseEntity<?> getManagers(@RequestParam(required = false) String type, ServerHttpRequest request) {
        List<Manager> managers = mdsContextService.getManagers();
        if (StringUtils.hasText(type)) {
            List<Manager> filteredManagers = managers.stream()
                .filter(manager -> type.equalsIgnoreCase(manager.getType().toString()))
                .collect(Collectors.toList());
            return ResponseEntity.ok(ManagerMapper.toDto(filteredManagers));
        } else {
            return ResponseEntity.ok(ManagerMapper.toDto(managers));
        }
    }

    @GetMapping("/managers/{name}")
    public ResponseEntity<?> getManagerByName(@PathVariable String name, ServerHttpRequest request) {
        return mdsContextService.getManager(name)
            .map(ManagerMapper::toDto)
            .<ResponseEntity<?>>map(ResponseEntity::ok)
            .orElseGet(() -> buildResponseEntity(request, HttpStatus.NOT_FOUND, "Manager[" + name + "] not found"));
    }

    @GetMapping("/managers/{name}/nes")
    public ResponseEntity<?> getNesByManager(@PathVariable String name, ServerHttpRequest request) {
        return mdsContextService.getDevicesByManager(name)
            .map(DeviceMapper::toDto)
            .<ResponseEntity<?>>map(ResponseEntity::ok)
            .orElseGet(() -> buildResponseEntity(request, HttpStatus.NOT_FOUND, "Manager[" + name + "] not found"));
    }

    @GetMapping("/nes/{identifier}")
    public ResponseEntity<?> getNeByIdentifier(@PathVariable String identifier, ServerHttpRequest request) {
        return mdsContextService.getDevice(identifier)
            .map(DeviceMapper::toDto)
            .<ResponseEntity<?>>map(ResponseEntity::ok)
            .orElseGet(
                () -> buildResponseEntity(request, HttpStatus.NOT_FOUND, "Ne[" + identifier + "] not found"));
    }

    @GetMapping("/nes/{identifier}/manager")
    public ResponseEntity<?> getManagerByNe(@PathVariable String identifier, ServerHttpRequest request) {
        Optional<Device> device = mdsContextService.getDevice(identifier);
        if (device.isEmpty()) {
            return buildResponseEntity(request, HttpStatus.NOT_FOUND, "Ne[" + identifier + "] not found");
        }

        return mdsContextService
            .getManager(device.get().getManagerName())
            .map(ManagerMapper::toDto)
            .<ResponseEntity<?>>map(ResponseEntity::ok)
            .orElseGet(
                () -> buildResponseEntity(request, HttpStatus.NOT_FOUND,
                    "Manager[device=" + identifier + "] not found"));
    }

    @GetMapping("/nes")
    public ResponseEntity<?> getNeByNameOrIp(@RequestParam(required = false) String name,
        @RequestParam(required = false) String ip, ServerHttpRequest request) {
        if (StringUtils.hasText(name)) {
            return mdsContextService.getDevice(name)
                .map(DeviceMapper::toDto)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(
                    () -> buildResponseEntity(request, HttpStatus.NOT_FOUND, "Ne[name=" + name + "] not found"));
        }

        if (StringUtils.hasText(ip)) {
            return mdsContextService.getDevice(ip)
                .map(DeviceMapper::toDto)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> buildResponseEntity(request, HttpStatus.NOT_FOUND, "Ne[ip=" + ip + "] not found"));
        }

        return buildResponseEntity(request, HttpStatus.BAD_REQUEST,
            "Either 'name' or 'ip' parameter must be provided");

    }

    private ResponseEntity<?> buildResponseEntity(ServerHttpRequest request, HttpStatus httpStatus, String msg) {
        Map<String, Object> errorAttributes = new LinkedHashMap<>();
        errorAttributes.put("timestamp", new Date());
        errorAttributes.put("path", request.getPath().pathWithinApplication().value());
        errorAttributes.put("status", httpStatus.value());
        errorAttributes.put("error", httpStatus.getReasonPhrase());
        errorAttributes.put("message", msg);
        errorAttributes.put("requestId", request.getId());

        return ResponseEntity.status(httpStatus).body(errorAttributes);
    }

}
