package com.synkrato.services.partner;

public final class SwaggerConstants {
  // API Info
  public static final String PARTNER_API_INFO_TITLE = "Product";
  public static final String PARTNER_API_INFO_DESCRIPTION =
      "Provides RESTful APIs for managing partner products.";
  public static final String PARTNER_API_INFO_TERMS_OF_USE_URL = "";
  public static final String PARTNER_API_INFO_CONTACT_NAME = "Encompass Partner Connect";
  public static final String PARTNER_API_INFO_CONTACT_URL =
      "https://docs.partnerconnect.synkrato.com/";
  public static final String PARTNER_API_INFO_CONTACT_EMAIL = "EPC-Platform-Team@ice.com";
  public static final String PARTNER_API_INFO_LICENSE = "";
  public static final String PARTNER_API_INFO_LICENSE_URL = "";

  // Products
  public static final String PRODUCT_CONTROLLER_SWAGGER_TAG = "Product";
  public static final String PRODUCT_CONTROLLER_SWAGGER_VALUE = "Product operations";

  public static final String PRODUCT_OPERATION_CREATE_SWAGGER_SUMMARY = "Register a new product";
  public static final String PRODUCT_OPERATION_CREATE_SWAGGER_DESCRIPTION =
      "Create a new product listing and initialize its configuration/entitlements. Only partners can register a new product.";

  public static final String PRODUCT_OPERATION_GET_SPECIFIC_SWAGGER_SUMMARY =
      "Retrieve a specific product";
  public static final String PRODUCT_OPERATION_GET_SPECIFIC_SWAGGER_DESCRIPTION =
      "Retrieve a specific products listing, entitlements and configuration information.";

  public static final String PRODUCT_OPERATION_GET_ALL_SWAGGER_SUMMARY =
      "Retrieve all registered products";
  public static final String PRODUCT_OPERATION_GET_ALL_SWAGGER_DESCRIPTION =
      "Retrieve all registered products information.";

  public static final String PRODUCT_OPERATION_UPDATE_SWAGGER_SUMMARY = "Update a specific product";
  public static final String PRODUCT_OPERATION_UPDATE_SWAGGER_DESCRIPTION =
      "Update a specific products listing, entitlements and configuration information.";

  // Billing Rules
  public static final String BILLING_RULES_CONTROLLER_SWAGGER_TAG = "Billing Rules";
  public static final String BILLING_RULES_CONTROLLER_SWAGGER_VALUE = "Billing Rules' operations";

  public static final String BILLING_RULES_OPERATION_CREATE_SWAGGER_SUMMARY =
      "Register a product billing rule";
  public static final String BILLING_RULES_OPERATION_CREATE_SWAGGER_DESCRIPTION =
      "Create a new product billing rule. Only partners can register a new product billing rule.";

  public static final String BILLING_RULES_OPERATION_UPDATE_SWAGGER_SUMMARY =
      "Update a specific product billing rule";
  public static final String BILLING_RULES_OPERATION_UPDATE_SWAGGER_DESCRIPTION =
      "Update a specific product billing rule. Only partners can update a product billing rule.";
  public static final String ACCESS_LEVEL = "accessLevel";
  public static final String ACCESS_POLICY = "access-policy";
  public static final String INTERNAL = "internal";

  private SwaggerConstants() {}
}
