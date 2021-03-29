package com.llk.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * author: llk
 * group: JJStudio
 */
@Retention(RetentionPolicy.SOURCE)
@Target(value = ElementType.METHOD)
public @interface BindClick {
    int[] ids();
}
