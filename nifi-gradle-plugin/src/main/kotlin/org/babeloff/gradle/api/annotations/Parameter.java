package org.babeloff.gradle.api.annotations;

public @interface Parameter
{
    String defaultValue() default "";

    boolean readonly() default false;

    boolean required() default false;

    String property() default "";

    String alias() default "";
}
