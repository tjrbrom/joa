package com.x.hhqs.joa.common.restclient.annotation;

import com.x.hhqs.joa.common.restclient.RequestMethod;

import javax.ws.rs.core.MediaType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface RestClient {
    RequestMethod method() default RequestMethod.GET;

    String mediaType() default MediaType.APPLICATION_JSON;

    String path();

    String acceptMediaType() default MediaType.APPLICATION_JSON;

    boolean byPassCB() default false;
}
