package com.x.hhqs.joa.in.vo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface NbiResponseDefinition {
	String fieldName();

	String nbiSpec() default "";

	boolean ignored() default false;
}
