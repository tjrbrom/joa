package com.x.hhqs.joa.tools.nacos.rest;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.x.hhqs.joa.tools.nacos.NacosUtils;

public class NacosRestClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(NacosRestClient.class);

    public static final String HEALTH_ENDPOINT = "/nacos/v2/core/cluster/node/self/health";

    public static final String CONFIG_ENDPOINT = "/nacos/v2/cs/config";

    public static final String CONFIG_LIST_ENDPOINT = "/nacos/v2/cs/history/configs?namespaceId=";

    private String nacosUrl;

    private HttpClient client;

    private ObjectMapper objectMapper = new ObjectMapper();

    public NacosRestClient(String nacosUrl) {
        this.nacosUrl = nacosUrl;
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private NacosResponse parseBody(String content) throws Exception {
        return objectMapper.readValue(content, NacosResponse.class);
    }

    public boolean checkServerHealth() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(nacosUrl + HEALTH_ENDPOINT))
                .GET()
                .timeout(Duration.ofSeconds(5))
                .build();

        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            NacosResponse nsp = parseBody(response.body());
            if ("UP".equalsIgnoreCase(nsp.getData())) {
                return true;
            }
        }

        LOGGER.error("Check health response: {} {}", response.statusCode(), response.body());
        return false;
    }

    public List<NacosConfig> getConfigsInNamespace(String namespaceId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(nacosUrl + CONFIG_LIST_ENDPOINT + namespaceId))
                .GET()
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(5))
                .build();

        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            String data = response.body().substring(response.body().indexOf("["), response.body().length() - 1);
            List<NacosConfig> configs = Arrays.asList(objectMapper.readValue(data, NacosConfig[].class));
            configs.stream().forEach(c -> c.setNamespaceId(namespaceId));
            return configs;
        }

        String errorMsg = String.format("Invalid getConfigsInNamespace response: %d %s",
                response.statusCode(), response.body());
        throw new IncorrectHttpResponseException(errorMsg);
    }

    public Map<String, String> getConfigData(String dataId, String group, String namespaceId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getConfigUrl(dataId, group, namespaceId)))
                .GET()
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(5))
                .build();

        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            NacosResponse nsp = parseBody(response.body());
            return NacosUtils.contentToMap(nsp.getData());
        }

        String errorMsg = String.format("Invalid getConfigData response: %d %s",
                response.statusCode(), response.body());
        throw new IncorrectHttpResponseException(errorMsg);
    }

    public void updateOrCreateConfig(NacosConfig config) throws Exception {
        Map<String, String> formData = new HashMap<>();
        formData.put("dataId", config.getDataId());
        formData.put("group", config.getGroup());
        formData.put("namespaceId", config.getNamespaceId());
        formData.put("type", config.getType());
        formData.put("content", config.getContent());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(nacosUrl + CONFIG_ENDPOINT))
                .headers("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(getFormDataAsString(formData)))
                .timeout(Duration.ofSeconds(5))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            String errorMsg = String.format("Invalid updateConfig response: %d %s",
                    response.statusCode(), response.body());
            throw new IncorrectHttpResponseException(errorMsg);
        }
    }

    String getFormDataAsString(Map<String, String> formData) {
        StringBuilder formBodyBuilder = new StringBuilder();
        for (Map.Entry<String, String> singleEntry : formData.entrySet()) {
            if (formBodyBuilder.length() > 0) {
                formBodyBuilder.append("&");
            }
            formBodyBuilder.append(URLEncoder.encode(singleEntry.getKey(), StandardCharsets.UTF_8));
            formBodyBuilder.append("=");
            formBodyBuilder.append(URLEncoder.encode(singleEntry.getValue(), StandardCharsets.UTF_8));
        }
        return formBodyBuilder.toString();
    }

    String getConfigUrl(String dataId, String group, String namespaceId) {
        String query = String.format("?dataId=%s&group=%s&namespaceId=%s",
                dataId, group, namespaceId);
        return getNacosUrl() + CONFIG_ENDPOINT + query;
    }

    public String getNacosUrl() {
        return nacosUrl;
    }

}
