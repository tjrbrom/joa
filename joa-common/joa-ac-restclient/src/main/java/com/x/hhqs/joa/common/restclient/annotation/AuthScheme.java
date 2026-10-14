package com.x.hhqs.joa.common.restclient.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declaring AuthType for rest method as Basic or Bearer
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface AuthScheme {
    AuthType value() default AuthType.BEARER;
}
