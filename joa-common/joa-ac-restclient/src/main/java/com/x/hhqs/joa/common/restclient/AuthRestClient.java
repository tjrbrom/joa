package com.x.hhqs.joa.common.restclient;

public interface AuthRestClient {
    TokenResponse login();

    void logout();

    TokenResponse refreshToken(String refreshToken);

    TokenResponse login(String grantType);

    void logout(String token, String tokenTypeHint);

    TokenResponseWrapper getToken();
}
