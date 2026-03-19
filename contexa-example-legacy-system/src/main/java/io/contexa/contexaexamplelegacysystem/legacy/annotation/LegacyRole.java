package io.contexa.contexaexamplelegacysystem.legacy.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Legacy role-based authorization annotation.
 * Used by LegacyAuthorizationInterceptor to check access.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface LegacyRole {
    String[] value();
}
