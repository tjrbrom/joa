package com.x.hhqs.joa.mds.emsclient.ams;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;


public class AmsRestClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(AmsRestClient.class);

    private ObjectMapper objectMapper;

    private WebClient webClient;

    private final AmsRestClientConfig clientConfig;
    private final String baseUrl;

    String sessionId;

    public AmsRestClient(AmsRestClientConfig clientConfig) {
        this.clientConfig = clientConfig;
        this.baseUrl = clientConfig.getBaseUrl();
    }

    public void init() {
        try {
            this.sessionId = getSessionId();
        } catch (Exception e) {
            LOGGER.info("baseUrl [{}], username [{}]", clientConfig.getBaseUrl(), clientConfig.getUsername());
            throw new RuntimeException("Get sessionId failed for " + clientConfig.getServerName(), e);
        }
    }

    public void close() {
        try {
            logout();
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    public Map<String, Map<String, Object>> getAttributesByFriendlyNames(Set<String> friendlyNames,
        Set<String> attributeNames) throws Exception {
        String url = "/webfwk/rest/mobjects/friendlyName/attributes/search?" + getQueryParams(attributeNames);

        Mono<String> responseMono = webClient.post()
            .uri(baseUrl + url)
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.COOKIE, sessionId)
            .bodyValue(friendlyNames)
            .retrieve()
            .onStatus(status -> !status.is2xxSuccessful(),
                clientResponse -> clientResponse.createException().flatMap(Mono::error))
            .bodyToMono(String.class);

        Map<String, Map<String, Object>> values =
            objectMapper.readValue(responseMono.block(), new TypeReference<Map<String, Map<String, Object>>>() {});

        return values;
    }

    private String getQueryParams(Set<String> attributeNames) {
        return attributeNames.stream()
            .map(v -> "attributeNames=" + v)
            .collect(Collectors.joining("&"));
    }

    private String getSessionId() throws Exception {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("j_username", clientConfig.getUsername());
        formData.add("j_password", clientConfig.getPassword());

        return webClient.post()
            .uri(baseUrl + "/webfwk/j_security_check")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue(formData)
            .exchangeToMono(response -> {
                if (response.statusCode().is2xxSuccessful() || response.statusCode().is3xxRedirection()) {
                    String sessionId = getJSessionId(response.headers().header(HttpHeaders.SET_COOKIE));

                    return validateSession(sessionId)
                        .flatMap(valid -> valid ? Mono.just(sessionId)
                            : Mono.error(new Exception("Session validation failed")));
                }
                return Mono.error(new Exception("Authentication failed for " + clientConfig.getServerName()));
            })
            .block();
    }

    private String getJSessionId(List<String> setCookies) {
        for (String setCookie : setCookies) {
            if (setCookie.contains("JSESSIONID")) {
                return setCookie.split(";")[0];
            }
        }

        return null;
    }

    private Mono<Boolean> validateSession(String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            return Mono.just(false);
        }
        return webClient.get()
            .uri(baseUrl + "/webfwk/rest/users/currentUser?fields=id")
            .header(HttpHeaders.COOKIE, sessionId)
            .retrieve()
            .bodyToMono(String.class)
            .map(responseBody -> {
                try {
                    JsonNode jsonNode = objectMapper.readTree(responseBody);
                    return jsonNode.has("id");
                } catch (Exception e) {
                    LOGGER.error("Validate Session failed for sessionId {}: {}", sessionId, e.getMessage());
                    return false;
                }
            })
            .onErrorReturn(false);
    }

    private void logout() {
        if (!StringUtils.hasText(sessionId)) {
            return;
        }
        webClient.get()
            .uri(baseUrl + "/webfwk/rest/auth/logout")
            .header(HttpHeaders.COOKIE, sessionId)
            .retrieve()
            .onStatus(status -> !status.is2xxSuccessful(),
                clientResponse -> clientResponse.createException().flatMap(Mono::error))
            .toBodilessEntity()
            .doOnSuccess(response -> LOGGER.debug("Logout successfully from {}", clientConfig.getServerName()))
            .doOnError(error -> LOGGER.warn("Logout failed from " + clientConfig.getServerName(), error))
            .block();
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void setWebClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public AmsRestClientConfig getClientConfig() {
        return this.clientConfig;
    }

}
