package com.x.hhqs.joa.alarm.controller;

import com.x.hhqs.joa.alarm.service.JoaAlarmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/joa-alarm/rest")
public class AlarmRestController {

    @Autowired
    JoaAlarmService joaAlarmService;

    @PostMapping("/findAlarmsByIds")
    public ResponseEntity<List<String>> findAlarmsByIds(@RequestBody List<String> alarmIds) {
        return ResponseEntity.ok(joaAlarmService.findAllAlarmsByIds(alarmIds));
    }
}
