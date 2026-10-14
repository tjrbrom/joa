package com.x.hhqs.joa.alarm.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CurrentUserVo {

    private String id;

    private String fullName;

    private String avatar;

    private String email;

    private String username;

    private Long inactivityTimeout;

    private List<String> roles;

}
