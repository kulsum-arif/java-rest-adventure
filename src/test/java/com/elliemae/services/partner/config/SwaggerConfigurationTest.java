package com.synkrato.services.partner.config;

import com.fasterxml.classmate.TypeResolver;
import junit.framework.TestCase;
import org.junit.Test;
import springfox.documentation.spring.web.plugins.Docket;

public class SwaggerConfigurationTest extends TestCase {

  @Test
  public void testSwaggerConfig() {
    SwaggerConfiguration swaggerConfiguration = new SwaggerConfiguration();

    Docket docket = swaggerConfiguration.api(new TypeResolver());

    assertNotNull(docket);
  }
}
