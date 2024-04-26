package com.synkrato.services.partner.security;

import com.synkrato.services.partner.enums.Permission;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation to annotate methods in the web layer to define the permission required.
 *
 * @author bappala
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Authorization {
  Permission permission();
}
