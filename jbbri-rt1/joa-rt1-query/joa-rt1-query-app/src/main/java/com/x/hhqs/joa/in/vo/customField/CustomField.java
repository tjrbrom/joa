package com.x.hhqs.joa.in.vo.customField;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CustomField {
	String ossName() default "";

	String fieldName() default "";

}
