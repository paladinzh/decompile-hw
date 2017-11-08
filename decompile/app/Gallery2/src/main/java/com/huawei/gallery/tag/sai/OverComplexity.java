package com.huawei.gallery.tag.sai;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.CONSTRUCTOR, ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface OverComplexity {
    int complexity();

    int maxDepth() default 1;

    int statements() default 1;
}
