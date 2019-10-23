package org.babeloff.gradle.api.annotations;

public @interface Component
{
    Class role() default java.lang.Object.class;
    String hint() default "n/a";
}
