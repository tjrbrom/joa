package com.x.hhqs.joa.common.restclient.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is a param annotation used to override the base url (E.g: 10.10.10.10) with an effective url (E.g: 10.10.10.10:8554).
 * This will help us reuse the same clientConfig (E.g: Token info) for each effective url without creating a new clientConfig for them.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface EffectiveBaseUrl {
}
