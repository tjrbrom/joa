package com.x.hhqs.joa.mds.utils;

import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import reactor.netty.http.client.HttpClient;

public class WebContextUtils {
    private WebContextUtils() {}

    private static class Holder {
        private static final SslContext INSTANCE = createSslContext();
    }

    public static SslContext getSslContext() {
        return Holder.INSTANCE;
    }

    private static SslContext createSslContext() {
        try {
            return SslContextBuilder.forClient()
                .trustManager(new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {}

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {}

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                })
                .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create SSL context", e);
        }
    }

    public static WebClient createWebClientWithoutSSL() {
        SslContext sslContext = getSslContext();
        HttpClient httpClient = HttpClient.create().secure(ssl -> ssl.sslContext(sslContext));

        return WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();
    }
}
