package com.synkrato.services.partner.config;

import static com.synkrato.services.epc.common.EpcCommonConstants.JWT_CLAIM_ISSUER;
import com.synkrato.services.partner.SwaggerConstants;
import com.synkrato.services.epc.common.dto.BillingRuleDTO;
import com.synkrato.services.epc.common.dto.ProductDTO;
import com.fasterxml.classmate.TypeResolver;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.Contact;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
public class SwaggerConfiguration {

  private static final String CONTROLLERS_BASE_PKG = "com.synkrato.services.partner.controller";

  @Bean
  public Docket api(TypeResolver typeResolver) {
    return new Docket(DocumentationType.SWAGGER_2)
        .select()
        .apis(RequestHandlerSelectors.basePackage(CONTROLLERS_BASE_PKG))
        .paths(PathSelectors.any())
        .paths(PathSelectors.any())
        .build()
        .additionalModels(
            typeResolver.resolve(BillingRuleDTO.class), typeResolver.resolve(ProductDTO.class))
        .apiInfo(apiInfo())
        .securityContexts(Collections.singletonList(securityContext()))
        .securitySchemes(Collections.singletonList(apiKey()));
  }

  private SecurityContext securityContext() {
    return SecurityContext.builder().securityReferences(defaultAuth()).build();
  }

  private List<SecurityReference> defaultAuth() {
    AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
    AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
    authorizationScopes[0] = authorizationScope;
    return Arrays.asList(new SecurityReference("JWT", authorizationScopes));
  }

  private ApiKey apiKey() {
    return new ApiKey("JWT", "Authorization", "header");
  }

  protected ApiInfo apiInfo() {
    return new ApiInfo(
        SwaggerConstants.PARTNER_API_INFO_TITLE,
        SwaggerConstants.PARTNER_API_INFO_DESCRIPTION,
        getClass().getPackage().getImplementationVersion(),
        SwaggerConstants.PARTNER_API_INFO_TERMS_OF_USE_URL,
        new Contact(
            SwaggerConstants.PARTNER_API_INFO_CONTACT_NAME,
            SwaggerConstants.PARTNER_API_INFO_CONTACT_URL,
            SwaggerConstants.PARTNER_API_INFO_CONTACT_EMAIL),
        SwaggerConstants.PARTNER_API_INFO_LICENSE,
        SwaggerConstants.PARTNER_API_INFO_LICENSE_URL,
        Collections.emptyList());
  }
}
