package com.synkrato.services.partner.config;

import org.hibernate.dialect.PostgreSQL95Dialect;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.type.BooleanType;

/**
 * NOTE: Hibernate Dialects are chained via inheritance. MaximumInheritanceDepth warning is
 * suppressed since PostgreSQL95Dialect is latest (as of writing) but inheritance level is too deep
 * when PostgreSQL95Dialect is used.
 *
 * @author rarora2
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class CustomDialect extends PostgreSQL95Dialect {

  public static final String CUSTOM_JSONB_CONTAINS = "custom_jsonb_contains";

  /** Constructor for CustomDialect */
  public CustomDialect() {
    registerFunction(
        CUSTOM_JSONB_CONTAINS,
        new SQLFunctionTemplate(BooleanType.INSTANCE, "?1 @> cast(?2 as jsonb)"));
  }
}
