package com.x.hhqs.joa.alarm.utils;

import com.x.hhqs.joa.alarm.dto.CurrentUserVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@Component
@Slf4j
public class RestWebClient {

    @Autowired
    @Qualifier("RestTemplateNoSSL")
    private RestTemplate restTemplateNoSSL;

    public static final String APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";

    public CurrentUserVo getCurrentUser(String coreAcBaseUrl, String path, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(APPLICATION_JSON_VALUE));
        headers.setBearerAuth(token);
        ResponseEntity<CurrentUserVo> currentUserVoResponseEntity = restTemplateNoSSL.exchange(coreAcBaseUrl + path, HttpMethod.GET,
                new HttpEntity<>(null, headers),
                CurrentUserVo.class
        );
        return HttpStatus.OK.equals(currentUserVoResponseEntity.getStatusCode()) ? currentUserVoResponseEntity.getBody() : null;
    }

    public String login(String authServerUrl, String path,  MultiValueMap<String, String> params) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        ResponseEntity<String> responseEntity = restTemplateNoSSL.postForEntity(
                authServerUrl + path,
                new HttpEntity<>(params, headers),
                String.class
        );
        return responseEntity.getStatusCode().is2xxSuccessful() ? responseEntity.getBody() : null;
    }
}

    /*public static String login(String authServerUrl, String path,  MultiValueMap<String, String> params) {
        WebClient.Builder builder = org.springframework.web.reactive.function.client.WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE, CHARSET_UTF_8)
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
                        .secure(t -> t.sslContext(
                                SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE)
                        ))))
                .baseUrl(authServerUrl);
        WebClient webClient = builder.build();
        ResponseEntity<String> subEntity = webClient.post()
                .uri(path)
                .header(HttpHeaders.CONTENT_TYPE, APPLICATION_X_WWW_FORM_URLENCODED)
                .bodyValue(params)
                .retrieve()
                .toEntity(String.class).block();
        if (subEntity == null) {
            return null;
        }

        String body = subEntity.getBody();
        int statusCode = subEntity.getStatusCodeValue();
        if (statusCode == 200 && body != null) {
            return body;
        }
        return null;
    }*/
//}
