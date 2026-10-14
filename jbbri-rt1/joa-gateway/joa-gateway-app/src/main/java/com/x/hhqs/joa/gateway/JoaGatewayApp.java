package com.x.hhqs.joa.gateway;


import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class JoaGatewayApp {

    public static void main(String[] args) throws Exception {
        new SpringApplicationBuilder(JoaGatewayApp.class)
            .web(WebApplicationType.REACTIVE)
            .run(args);
    }

}

