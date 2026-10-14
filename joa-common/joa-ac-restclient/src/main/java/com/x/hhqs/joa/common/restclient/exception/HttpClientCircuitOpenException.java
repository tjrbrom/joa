package com.x.hhqs.joa.common.restclient.exception;

public class HttpClientCircuitOpenException extends CircuitBreakerException {
    public HttpClientCircuitOpenException() {
        super("Connection to Http/Https server is down!");
    }

    public HttpClientCircuitOpenException(Throwable cause) {
        super("Http Circuit Breaker has detected that connection to Http/Https server is down!", cause);
    }
}
