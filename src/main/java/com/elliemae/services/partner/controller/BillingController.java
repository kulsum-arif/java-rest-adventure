package com.synkrato.services.partner.controller;

import static com.synkrato.services.epc.common.EpcCommonConstants.APPLICATION_MERGE_PATCH_JSON_TYPE;
import static com.synkrato.services.epc.common.EpcCommonConstants.ID_VIEW;
import static com.synkrato.services.epc.common.EpcCommonConstants.LOGGER_START;
import static com.synkrato.services.epc.common.EpcCommonConstants.VIEW_DEFAULT;
import static com.synkrato.services.partner.controller.BaseController.PRODUCT_CONTEXT;
import static com.synkrato.services.partner.controller.BaseController.VERSION;

import com.synkrato.services.epc.common.dto.BillingRuleDTO;
import com.synkrato.services.epc.common.error.EpcRuntimeException;
import com.synkrato.services.epc.common.validator.BillingCreateGroup;
import com.synkrato.services.epc.common.views.Views;
import com.synkrato.services.partner.SwaggerConstants;
import com.synkrato.services.partner.business.BillingRuleService;
import com.synkrato.services.partner.dto.ProductQueryDTO;
import com.synkrato.services.partner.enums.EntityView;
import com.synkrato.services.partner.enums.Permission;
import com.synkrato.services.partner.security.Authorization;
import com.synkrato.services.partner.util.ProductUtil;
import com.synkrato.services.partner.validators.BillingRuleValidator;
import com.synkrato.services.partner.validators.ProductQueryValidator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Api(
    tags = SwaggerConstants.BILLING_RULES_CONTROLLER_SWAGGER_TAG,
    value = SwaggerConstants.BILLING_RULES_CONTROLLER_SWAGGER_VALUE)
@RestController
@RequestMapping(VERSION + PRODUCT_CONTEXT)
public class BillingController extends BaseController {

  @Autowired private BillingRuleService billingRuleService;
  @Autowired private BillingRuleValidator billingRuleValidator;
  @Autowired private ProductQueryValidator productQueryValidator;
  @Autowired private ProductUtil productUtil;

  @ApiOperation(
      value = SwaggerConstants.BILLING_RULES_OPERATION_CREATE_SWAGGER_SUMMARY,
      notes = SwaggerConstants.BILLING_RULES_OPERATION_CREATE_SWAGGER_DESCRIPTION,
      nickname = "createBillingRuleUsingPOST",
      extensions = {
        @Extension(
            name = SwaggerConstants.ACCESS_POLICY,
            properties = {
              @ExtensionProperty(
                  name = SwaggerConstants.ACCESS_LEVEL,
                  value = SwaggerConstants.INTERNAL)
            })
      })
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content = {
        @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = BillingRuleDTO.class))
      })
  @ApiImplicitParams({
    @ApiImplicitParam(
        name = VIEW_REQUEST_PARAM,
        value = "View",
        dataTypeClass = String.class,
        paramType = "query",
        allowableValues = VIEW_REQUEST_PARAM_ID)
  })
  @PostMapping(
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE,
      value = PRODUCT_BILLING_CREATE_URI)
  @Authorization(permission = Permission.CREATE_PRODUCT)
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseEntity<Object> createBillingRule(
      @PathVariable(PRODUCT_ID_PATH_PARAM) String productId,
      @RequestBody @Validated(BillingCreateGroup.class) @Valid BillingRuleDTO billingRule,
      @Validated ProductQueryDTO productQueryDTO,
      HttpServletRequest request) {

    productUtil.buildProductQueryView(productQueryDTO, ID_VIEW);
    billingRule.setProductId(productId);
    BillingRuleDTO newBillingRule = billingRuleService.create(billingRule);
    URI location = getResourceURI(request, newBillingRule.getId());
    return ResponseEntity.status(HttpStatus.CREATED)
        .location(location)
        .body(resolveViewForBilling(newBillingRule, getEntityView(productQueryDTO.getView())));
  }

  @ApiOperation(
      value = SwaggerConstants.BILLING_RULES_OPERATION_UPDATE_SWAGGER_SUMMARY,
      notes = SwaggerConstants.BILLING_RULES_OPERATION_UPDATE_SWAGGER_DESCRIPTION,
      nickname = "updateBillingRuleUsingPATCH",
      extensions = {
        @Extension(
            name = SwaggerConstants.ACCESS_POLICY,
            properties = {
              @ExtensionProperty(
                  name = SwaggerConstants.ACCESS_LEVEL,
                  value = SwaggerConstants.INTERNAL)
            })
      })
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content = {
        @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = BillingRuleDTO.class))
      })
  @ApiImplicitParams({
    @ApiImplicitParam(
        name = VIEW_REQUEST_PARAM,
        value = "View",
        dataTypeClass = String.class,
        paramType = "query",
        allowableValues =
            VIEW_REQUEST_PARAM_ID + "," + VIEW_DEFAULT + "," + VIEW_REQUEST_PARAM_COMPLETE)
  })
  @PatchMapping(
      consumes = APPLICATION_MERGE_PATCH_JSON_TYPE,
      produces = MediaType.APPLICATION_JSON_VALUE,
      value = PRODUCT_BILLING_UPDATE_URI)
  @Authorization(permission = Permission.UPDATE_PRODUCT)
  @ResponseStatus(HttpStatus.OK)
  public ResponseEntity<Object> updateBillingRule(
      @PathVariable(value = PRODUCT_ID_PATH_PARAM) String productId,
      @PathVariable(value = BILLING_RULE_ID_PATH_PARAM) String id,
      @RequestBody Map<String, Object> updateBillingRuleMap,
      @Validated ProductQueryDTO productQueryDTO)
      throws MethodArgumentNotValidException {

    productUtil.buildProductQueryView(productQueryDTO, ID_VIEW);
    BillingRuleDTO mergedBillingRuleDto =
        billingRuleService.update(productId, id, updateBillingRuleMap);

    return ResponseEntity.status(HttpStatus.OK)
        .body(
            resolveViewForBilling(mergedBillingRuleDto, getEntityView(productQueryDTO.getView())));
  }

  /**
   * This method will return views
   *
   * @param billingRuleDTO incoming billing rule
   * @param entityView view that is being requested
   * @return output for the given view
   */
  private String resolveViewForBilling(BillingRuleDTO billingRuleDTO, EntityView entityView) {
    log.debug("resolve_view_for_billing {}", LOGGER_START);

    try {
      switch (entityView) {
        case ID:
          return objectMapper.writerWithView(Views.Id.class).writeValueAsString(billingRuleDTO);
        case COMPLETE:
          return objectMapper
              .writerWithView(Views.Complete.class)
              .writeValueAsString(billingRuleDTO);
        default:
          return objectMapper
              .writerWithView(Views.Default.class)
              .writeValueAsString(billingRuleDTO);
      }
    } catch (IOException e) {
      log.error("Failed while resolving view {}", e.getMessage(), e);
      throw new EpcRuntimeException(MARSHALL_UNMARSHALL_ERROR, e);
    }
  }

  @InitBinder(BILLING_RULE_DTO)
  void initBinderBillingRule(final ServletRequestDataBinder binder) {
    binder.addValidators(billingRuleValidator);
  }

  @InitBinder(PRODUCT_QUERY_DTO)
  void initBinderProductQuery(final ServletRequestDataBinder binder) {
    binder.addValidators(productQueryValidator);
  }
}
