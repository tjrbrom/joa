package com.x.hhqs.joa.alarm.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.x.hhqs.joa.alarm.config.KeyCloakClientConfig;
import com.x.hhqs.joa.alarm.dto.CurrentUserVo;
import com.x.hhqs.joa.alarm.service.KeyCloakClientService;
import com.x.hhqs.joa.alarm.utils.RestWebClient;
import com.x.hhqs.joa.alarm.vo.KeycloakTokenVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;



@Service
@Slf4j
public class KeyCloakClientServiceImpl implements KeyCloakClientService {

    public static final String GRANT_TYPE = "grant_type";

    public static final String CLIENT_ID = "client_id";

    public static final String CLIENT_SECRET = "client_secret";

    public static final String REFRESH_TOKEN = "refresh_token";

    public static final String USERNAME = "username";

    public static final String PASSWORD = "password";

    private final KeyCloakClientConfig keyCloakClientConfig;

    private final String coreAcBaseUrl;

    private final String currentUserUrl;

    private final ObjectMapper objectMapper;

    private final RestWebClient restWebClient;

    public KeyCloakClientServiceImpl(@Value("${core.rest.baseUrl}") String coreAcBaseUrl,
                                     @Value("${core.rest.current-user-url}") String currentUserUrl,
                                     ObjectMapper objectMapper,
                                     KeyCloakClientConfig keyCloakClientConfig,
                                     RestWebClient restWebClient
                                     ) {
        this.coreAcBaseUrl = coreAcBaseUrl;
        this.currentUserUrl = currentUserUrl;
        this.objectMapper = objectMapper;
        this.keyCloakClientConfig = keyCloakClientConfig;
        this.restWebClient = restWebClient;
    }

    @Override
    public KeycloakTokenVo loginACKeycloakToken(String userName, String password) {
        if (StringUtils.isEmpty(userName) || StringUtils.isEmpty(password)) {
            return null;
        }
        KeycloakTokenVo keycloakTokenVo = this.getKeycloakTokenVo(PASSWORD, userName, password);
        if (keycloakTokenVo == null || StringUtils.isEmpty(keycloakTokenVo.getAccessToken())) return null;
        return keycloakTokenVo;
    }

    public KeycloakTokenVo getKeycloakTokenVo(String grantType, String userName, String password) {
        try {
            log.info("loginACKeycloakToken userName = {},", userName);
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add(GRANT_TYPE, grantType);
            params.add(CLIENT_ID, keyCloakClientConfig.getResource());
            params.add(CLIENT_SECRET, keyCloakClientConfig.getCredentials().getSecret());
            params.add(USERNAME, userName);
            params.add(PASSWORD, password);

            KeycloakTokenVo keycloakTokenVo = login(params);
            if (keycloakTokenVo == null) return null;
            CurrentUserVo currentUser = getCurrentUser(keycloakTokenVo.getAccessToken());
            keycloakTokenVo.setRoles(currentUser.getRoles());
            return keycloakTokenVo;
        } catch (Exception e) {
            log.error("login error {}", e.getMessage());
        }
        return null;
    }

    public CurrentUserVo getCurrentUser(String token) {
        return restWebClient.getCurrentUser(coreAcBaseUrl, currentUserUrl, token);
    }

    public KeycloakTokenVo login(MultiValueMap<String, String> params) throws Exception {
        String body = restWebClient.login(keyCloakClientConfig.getAuthServerUrl(), keyCloakClientConfig.getToken(), params);
        if (body != null) {
            return objectMapper.readValue(body, KeycloakTokenVo.class);
        }
        return null;
    }

}
