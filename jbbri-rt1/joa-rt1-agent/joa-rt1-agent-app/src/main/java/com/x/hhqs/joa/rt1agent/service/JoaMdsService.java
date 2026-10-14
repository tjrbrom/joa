package com.x.hhqs.joa.rt1agent.service;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.x.hhqs.joa.rt1agent.config.MdsConfig;

@Service
public class JoaMdsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(JoaMdsService.class);

    @Autowired
    private MdsConfig mdsConfig;

    @Autowired
    private RestTemplate restTemplate;

    public boolean isAmsDevice(String oltId) {
        String url = getMdsUrl(oltId);
        LOGGER.info("url: {}", url);

        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                JsonNode.class);

            LOGGER.debug("{}", response.getBody());
            if (response.getStatusCode() == HttpStatus.OK) {
                return Optional.ofNullable(response.getBody())
                    .map(b -> b.get("managerType"))
                    .map(JsonNode::asText)
                    .map("AMS"::equalsIgnoreCase)
                    .orElse(false);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    String getMdsUrl(String oltId) {
        if (mdsConfig.getBaseUrl().startsWith("http://")) {
            return mdsConfig.getBaseUrl() + "/api/v1/mds/nes/" + oltId;
        }
        return "http://" + mdsConfig.getBaseUrl() + "/api/v1/mds/nes/" + oltId;
    }

}
