package com.x.hhqs.joa.alarm.service;


import com.x.hhqs.joa.alarm.vo.KeycloakTokenVo;

public interface KeyCloakClientService {
    KeycloakTokenVo loginACKeycloakToken(String userName, String password);
}
