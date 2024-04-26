package com.synkrato.services.partner.controller;

import com.synkrato.services.epc.common.dto.ProductDTO;
import com.synkrato.services.epc.common.error.EpcRuntimeException;
import com.synkrato.services.epc.common.views.Views;
import com.synkrato.services.partner.enums.EntityView;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Slf4j
public abstract class BaseController {

  public static final String VERSION = "/v2";
  public static final String PRODUCT_CONTEXT = "/products";

  public static final String PRODUCT_ID_PATH_PARAM = "productId";
  public static final String PRODUCT_ID_PATH_PARAM_MAPPING = "{" + PRODUCT_ID_PATH_PARAM + "}";

  public static final String BILLING_RULE_ID_PATH_PARAM = "billingRuleId";
  public static final String BILLING_RULE_ID_PATH_PARAM_MAPPING =
      "{" + BILLING_RULE_ID_PATH_PARAM + "}";

  public static final String PRODUCT_BILLING_CREATE_URI =
      PRODUCT_ID_PATH_PARAM_MAPPING + "/billingRules";
  public static final String PRODUCT_BILLING_UPDATE_URI =
      PRODUCT_ID_PATH_PARAM_MAPPING + "/billingRules/" + BILLING_RULE_ID_PATH_PARAM_MAPPING;

  // Response views
  public static final String VIEW_REQUEST_PARAM = "view";
  public static final String VIEW_REQUEST_PARAM_ID = "id";
  public static final String VIEW_REQUEST_PARAM_INTEGRATION_TYPE = "integrationType";
  public static final String VIEW_REQUEST_PARAM_ENTITLEMENT = "entitlement";
  public static final String VIEW_REQUEST_PARAM_FINDINGS = "findings";
  public static final String VIEW_REQUEST_PARAM_SERVICEEVENTS = "serviceEvents";
  public static final String VIEW_REQUEST_PARAM_WEBHOOK = "webhook";
  public static final String VIEW_RESOURCES = "resources";
  public static final String VIEW_REQUEST_PARAM_COMPLETE = "complete";
  public static final String MARSHALL_UNMARSHALL_ERROR = "Unable to marshall: ";
  public static final String PARTNER_ID_PARAM = "partnerId";
  public static final String PRODUCT_SEARCH_ATTRIBUTE = "productSearchDTO";
  public static final String PRODUCT_QUERY_DTO = "productQueryDTO";
  public static final String PRODUCT_DTO = "productDTO";
  public static final String BILLING_RULE_DTO = "billingRuleDTO";
  public static final String RESPONSE_HEADER_ITEM_COUNT = "X-Total-Count";

  @Autowired protected ObjectMapper objectMapper;

  /**
   * @param response HttpServletResponse
   * @param key Response header key
   * @param value Response header value
   */
  public void addResponseHeader(HttpServletResponse response, String key, String value) {
    response.setHeader(key, value);
  }

  public String resolveView(ProductDTO productDTO, EntityView entityView) {

    try {
      switch (entityView) {
        case ID:
          return objectMapper.writerWithView(Views.Id.class).writeValueAsString(productDTO);
        case CREDENTIAL:
          return objectMapper.writerWithView(Views.Credential.class).writeValueAsString(productDTO);
        case INTEGRATIONTYPE:
          return objectMapper
              .writerWithView(Views.IntegrationType.class)
              .writeValueAsString(productDTO);
        case ENTITLEMENT:
          return objectMapper
              .writerWithView(Views.Entitlement.class)
              .writeValueAsString(productDTO);
        case OPTIONS:
          return objectMapper.writerWithView(Views.Options.class).writeValueAsString(productDTO);
        case WEBHOOK:
          return objectMapper.writerWithView(Views.Webhook.class).writeValueAsString(productDTO);
        case BILLINGRULES:
          return objectMapper
              .writerWithView(Views.BillingRules.class)
              .writeValueAsString(productDTO);
        case COMPLETE:
          return objectMapper.writeValueAsString(productDTO);
        case EXPORTS:
          return objectMapper.writerWithView(Views.Exports.class).writeValueAsString(productDTO);
        case RESOURCES:
          return objectMapper.writerWithView(Views.Resources.class).writeValueAsString(productDTO);
        case FINDINGS:
          return objectMapper.writerWithView(Views.Findings.class).writeValueAsString(productDTO);
        case SERVICEEVENTS:
          return objectMapper.writerWithView(Views.ServiceEvents.class).writeValueAsString(productDTO);
        case DEFAULT:
        default:
          return objectMapper.writerWithView(Views.Default.class).writeValueAsString(productDTO);
      }
    } catch (IOException e) {
      log.error("Failed while resolving view={} {}", entityView, e.getMessage(), e);
      throw new EpcRuntimeException(MARSHALL_UNMARSHALL_ERROR, e);
    }
  }

  public String resolveListViewProduct(
      List<? extends ProductDTO> entityDTOList, EntityView entityView) {

    try {
      switch (entityView) {
        case ID:
          return objectMapper.writerWithView(Views.Id.class).writeValueAsString(entityDTOList);
        case CREDENTIAL:
          return objectMapper
              .writerWithView(Views.Credential.class)
              .writeValueAsString(entityDTOList);
        case INTEGRATIONTYPE:
          return objectMapper
              .writerWithView(Views.IntegrationType.class)
              .writeValueAsString(entityDTOList);
        case ENTITLEMENT:
          return objectMapper
              .writerWithView(Views.Entitlement.class)
              .writeValueAsString(entityDTOList);
        case OPTIONS:
          return objectMapper.writerWithView(Views.Options.class).writeValueAsString(entityDTOList);
        case WEBHOOK:
          return objectMapper.writerWithView(Views.Webhook.class).writeValueAsString(entityDTOList);
        case COMPLETE:
          return objectMapper.writeValueAsString(entityDTOList);
        case DEFAULT:
        default:
          return objectMapper.writerWithView(Views.Default.class).writeValueAsString(entityDTOList);
      }
    } catch (IOException e) {
      log.error("Failed while resolving list view {}", e.getMessage(), e);
      throw new EpcRuntimeException(MARSHALL_UNMARSHALL_ERROR, e);
    }
  }

  /** Convert view string to EntityView */
  public EntityView getEntityView(String view) {
    EntityView entityView = EntityView.INTEGRATIONTYPE;

    if (!StringUtils.isEmpty(view)) {
      entityView = EntityView.findByCode(view);
    }

    return entityView;
  }

  URI getResourceURI(HttpServletRequest request, String string) {
    URI location = null;
    try {
      if (HttpMethod.POST.toString().equals(request.getMethod())) {
        location =
            ServletUriComponentsBuilder.fromRequest(request)
                .path("/" + string)
                .replaceQuery(null)
                .build()
                .toUri();
      } else {
        location =
            ServletUriComponentsBuilder.fromRequest(request).replaceQuery(null).build().toUri();
      }

    } catch (Exception e) {
      log.error("Failed while getting resource uri: {}", e.getMessage(), e);
      throw new EpcRuntimeException(e.getMessage(), e);
    }
    return location;
  }
}
