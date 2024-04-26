package com.synkrato.services.partner.util;

import static com.synkrato.services.epc.common.EpcCommonConstants.COLON;
import static com.synkrato.services.epc.common.EpcCommonConstants.CREATED_BY;
import static com.synkrato.services.epc.common.EpcCommonConstants.CREATED_DATE;
import static com.synkrato.services.epc.common.EpcCommonConstants.DML_INSERT;
import static com.synkrato.services.epc.common.EpcCommonConstants.DML_UPDATE_PATCH;
import static com.synkrato.services.epc.common.EpcCommonConstants.ENVIRONMENT;
import static com.synkrato.services.epc.common.EpcCommonConstants.ESCAPE_QUOTE;
import static com.synkrato.services.epc.common.EpcCommonConstants.JSON_PATH_LEFT_BRACKET;
import static com.synkrato.services.epc.common.EpcCommonConstants.JSON_PATH_RIGHT_BRACKET;
import static com.synkrato.services.epc.common.EpcCommonConstants.LEFT_CURLY_BRACKET;
import static com.synkrato.services.epc.common.EpcCommonConstants.LOGGER_END;
import static com.synkrato.services.epc.common.EpcCommonConstants.LOGGER_START;
import static com.synkrato.services.epc.common.EpcCommonConstants.PARTNER_ID;
import static com.synkrato.services.epc.common.EpcCommonConstants.PARTNER_TEST_ENVIRONMENT;
import static com.synkrato.services.epc.common.EpcCommonConstants.REQUEST_TYPE;
import static com.synkrato.services.epc.common.EpcCommonConstants.RIGHT_CURLY_BRACKET;
import static com.synkrato.services.epc.common.EpcCommonConstants.TENANT;
import static com.synkrato.services.epc.common.EpcCommonConstants.UPDATED_BY;
import static com.synkrato.services.epc.common.EpcCommonConstants.UPDATED_DATE;
import static com.synkrato.services.epc.common.EpcCommonConstants.VIEW_CREDENTIAL;
import static com.synkrato.services.partner.PartnerServiceConstants.ADDITIONAL_PROPERTIES;
import static com.synkrato.services.partner.PartnerServiceConstants.EMPTY_REGEX;
import static com.synkrato.services.partner.PartnerServiceConstants.EPC2;
import static com.synkrato.services.partner.PartnerServiceConstants.EXTENSIONS;
import static com.synkrato.services.partner.PartnerServiceConstants.EXTENSION_LIMIT;
import static com.synkrato.services.partner.PartnerServiceConstants.LISTING_NAME;
import static com.synkrato.services.partner.PartnerServiceConstants.REQUEST_TYPES_ATTRIBUTE;
import static java.util.stream.Collectors.joining;

import com.synkrato.components.microservice.identity.IdentityContext;
import com.synkrato.services.epc.common.dto.AccessEntitlementDTO;
import com.synkrato.services.epc.common.dto.AdditionalLinkDTO;
import com.synkrato.services.epc.common.dto.BillingRuleDTO;
import com.synkrato.services.epc.common.dto.ConditionDTO;
import com.synkrato.services.epc.common.dto.CredentialDTO;
import com.synkrato.services.epc.common.dto.EntitlementDTO;
import com.synkrato.services.epc.common.dto.ExportDTO;
import com.synkrato.services.epc.common.dto.FeatureDTO;
import com.synkrato.services.epc.common.dto.FieldDTO;
import com.synkrato.services.epc.common.dto.FindingDTO;
import com.synkrato.services.epc.common.dto.FindingTypeDTO;
import com.synkrato.services.epc.common.dto.ManifestDTO;
import com.synkrato.services.epc.common.dto.OptionsDTO;
import com.synkrato.services.epc.common.dto.PlmAttributeDef;
import com.synkrato.services.epc.common.dto.ProductDTO;
import com.synkrato.services.epc.common.dto.ResultDTO;
import com.synkrato.services.epc.common.dto.ServiceEventDTO;
import com.synkrato.services.epc.common.dto.ServiceEventTypeDTO;
import com.synkrato.services.epc.common.dto.TransactionEntitlementDTO;
import com.synkrato.services.epc.common.dto.UxDTO;
import com.synkrato.services.epc.common.dto.WebhookDTO;
import com.synkrato.services.epc.common.dto.enums.AccessScope;
import com.synkrato.services.epc.common.dto.enums.EnvironmentType;
import com.synkrato.services.epc.common.dto.enums.IntegrationType;
import com.synkrato.services.epc.common.dto.enums.ProductStatusType;
import com.synkrato.services.epc.common.dto.enums.SenderType;
import com.synkrato.services.epc.common.dto.enums.ServiceEventType;
import com.synkrato.services.epc.common.dto.enums.TagType;
import com.synkrato.services.epc.common.kafka.ProductKafkaEvent.Payload;
import com.synkrato.services.epc.common.util.CommonUtil;
import com.synkrato.services.epc.common.util.MessageUtil;
import com.synkrato.services.partner.business.BillingRuleService;
import com.synkrato.services.partner.business.webhook.SubscriptionService;
import com.synkrato.services.partner.data.AccessEntitlement;
import com.synkrato.services.partner.data.AdditionalLink;
import com.synkrato.services.partner.data.Condition;
import com.synkrato.services.partner.data.Credential;
import com.synkrato.services.partner.data.Export;
import com.synkrato.services.partner.data.Feature;
import com.synkrato.services.partner.data.Field;
import com.synkrato.services.partner.data.Finding;
import com.synkrato.services.partner.data.FindingType;
import com.synkrato.services.partner.data.Manifest;
import com.synkrato.services.partner.data.ManifestRequest;
import com.synkrato.services.partner.data.Options;
import com.synkrato.services.partner.data.Product;
import com.synkrato.services.partner.data.Result;
import com.synkrato.services.partner.data.ServiceEvent;
import com.synkrato.services.partner.data.ServiceType;
import com.synkrato.services.partner.data.TransactionEntitlement;
import com.synkrato.services.partner.data.Ux;
import com.synkrato.services.partner.data.jpa.domain.ProductPageRequest;
import com.synkrato.services.partner.dto.ProductListQueryDTO;
import com.synkrato.services.partner.dto.ProductQueryDTO;
import com.synkrato.services.partner.dto.ProductSearchDTO;
import com.synkrato.services.partner.enums.EntityView;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

@Component
@Slf4j
public class ProductUtil {

  private static final Set<EntityView> MANIFEST_VIEWS =
      new HashSet<>(
          Arrays.asList(
              EntityView.ENTITLEMENT,
              EntityView.COMPLETE,
              EntityView.EXPORTS,
              EntityView.RESOURCES,
              EntityView.FINDINGS,
              EntityView.SERVICEEVENTS,
              EntityView.DEFAULT));

  @Autowired AccessEntitlementUtil accessEntitlementUtil;

  @Value("${synkrato.epc.product.default-query-limit:25}")
  private int defaultQueryLimit;

  @Autowired private SubscriptionService subscriptionService;
  @Autowired private BillingRuleService billingRuleService;
  @Autowired private MessageUtil messageUtil;
  @Autowired private ObjectMapper objectMapper;

  /**
   * Returns the partner id, which is: 1. client id from the JWT, if its a partner token. OR 2. from
   * the payload in all other cases
   */
  public static String getPartnerId(ProductDTO productDTO) {

    return CommonUtil.hasPartnerToken() && StringUtils.isEmpty(productDTO.getPartnerId())
        ? IdentityContext.get().getClientId()
        : productDTO.getPartnerId();
  }

  /**
   * converts the product categories to upper case
   *
   * @param categories product categories
   */
  public static List<String> convertCategoriesToUpperCase(List<String> categories) {

    return !CollectionUtils.isEmpty(categories)
        ? categories.stream().map(String::toUpperCase).distinct().collect(Collectors.toList())
        : null;
  }

  /**
   * Build ProductDTO
   *
   * @param product Product Entity
   * @param productDTO Product Details
   * @return Product Details
   */
  public ProductDTO buildProductDTO(
      Product product, ProductDTO productDTO, boolean getWebhookSubscription, String view) {
    if (Objects.isNull(productDTO)) {
      productDTO = new ProductDTO();
    }
    BeanUtils.copyProperties(product, productDTO);
    productDTO.setId(product.getId().toString());
    productDTO.setStatus(ProductStatusType.valueOf(product.getStatus()));
    productDTO.setEnvironment(EnvironmentType.valueOf(product.getEnvironment()));
    productDTO.setIntegrationType(IntegrationType.valueOf(product.getIntegrationType()));
    productDTO.setCreated(CommonUtil.getISODateAsString(product.getCreated()));
    productDTO.setUpdated(CommonUtil.getISODateAsString(product.getUpdated()));

    if (Objects.nonNull(product.getCredentials())) {
      productDTO.setCredentials(buildCredentialDTO(product.getCredentials()));
    }

    productDTO.setFeature(buildFeatureDTO(product.getFeature()));

    if (isOptionsView(view) && !CollectionUtils.isEmpty(product.getOptions())) {
      productDTO.setOptions(buildOptionsDTO(product.getOptions()));
    }

    if (isWebhookSubscriptionRequired(getWebhookSubscription, view)) {
      productDTO.setWebhooks(buildWebhookDTO(product.getWebhookSubscriptions()));
    }

    ManifestDTO manifestDTO = null;
    if (isManifestView(view) && Objects.nonNull(product.getManifest())) {
      manifestDTO = buildManifestDTO(product.getManifest(), new ManifestDTO());
    }

    AccessEntitlementDTO accessEntitlementDTO = null;
    if (Objects.nonNull(product.getAccessEntitlements())) {
      accessEntitlementDTO = buildAccessEntitlementDTO(product.getAccessEntitlements());
    }

    EntitlementDTO entitlement =
        EntitlementDTO.builder().access(accessEntitlementDTO).data(manifestDTO).build();

    productDTO.setEntitlements(entitlement);

    // adding billing rules here. This will be only available with view=billingRules for now
    List<BillingRuleDTO> billingRuleDTOS = null;
    if (isBillingRulesView(view)) {
      billingRuleDTOS = billingRuleService.findBillingRules(product.getId().toString());
    }

    productDTO.setBillingRules(billingRuleDTOS);

    populateAdditionalLinks(productDTO, product);

    return productDTO;
  }

  /**
   * convert additionalLinks entities to dtos
   *
   * @param productDTO
   * @param product
   */
  private void populateAdditionalLinks(ProductDTO productDTO, Product product) {
    if (!CollectionUtils.isEmpty(product.getAdditionalLinks())) {
      productDTO.setAdditionalLinks(buildAdditionalLinksDTO(product.getAdditionalLinks()));
    }
  }

  /**
   * Build additional links dto from entity.
   *
   * @param entitiesList
   * @return
   */
  private List<AdditionalLinkDTO> buildAdditionalLinksDTO(List<AdditionalLink> entitiesList) {

    log.debug("build_additional_links_dto{}", LOGGER_START);
    if (CollectionUtils.isEmpty(entitiesList)) {
      return new ArrayList<>();
    }
    List<AdditionalLinkDTO> additionalLinkDTOList = new ArrayList<>();
    entitiesList.forEach(
        entity -> {
          AdditionalLinkDTO dto = AdditionalLinkDTO.builder().build();
          BeanUtils.copyProperties(entity, dto);
          additionalLinkDTOList.add(dto);
        });
    log.debug("build_additional_links_dto {}", LOGGER_END);
    return additionalLinkDTOList;
  }

  /**
   * Evaluates if webhook subscription is required:
   *
   * <pre>
   *   1. getWebhookSubscription is true
   *   2. view = complete or webhook
   *   3. has internal admin token or partner token
   * </pre>
   *
   * @param getWebhookSubscription
   * @param view
   * @return
   */
  private boolean isWebhookSubscriptionRequired(boolean getWebhookSubscription, String view) {

    return getWebhookSubscription && isWebhookView(view) && CommonUtil.hasValidProductUpdateToken();
  }

  /**
   * Build Product entity
   *
   * @param product Product Entity details
   * @param productDTO Product details
   * @param dmlAction Insert or update action
   * @return Product entity
   */
  public Product buildProduct(Product product, ProductDTO productDTO, String dmlAction) {

    log.debug("build_product: DML Action {}", LOGGER_START);

    BeanUtils.copyProperties(productDTO, product);
    product.setStatus(productDTO.getStatus().name());

    if (Objects.isNull(productDTO.getIntegrationType())) {
      product.setIntegrationType(IntegrationType.ASYNC.getDescription());
    } else {
      product.setIntegrationType(productDTO.getIntegrationType().name());
    }

    product.setFeature(buildFeature(productDTO.getFeature()));

    if (DML_INSERT.equals(dmlAction)) {
      product.setEnvironment(productDTO.getEnvironment().name());
    } else if (DML_UPDATE_PATCH.equals(dmlAction)) {
      product.setEnvironment(getEnvironment(productDTO.getStatus()).name());
    }

    product.setManifest(
        buildManifest(
            !hasDataEntitlements(productDTO.getEntitlements())
                ? new ManifestDTO()
                : productDTO.getEntitlements().getData(),
            Objects.isNull(product.getManifest()) ? new Manifest() : product.getManifest()));

    product.getManifest().setProduct(product);

    product.setAccessEntitlements(
        buildAccessEntitlement(
            !hasAccessEntitlements(productDTO.getEntitlements())
                ? new AccessEntitlementDTO()
                : productDTO.getEntitlements().getAccess(),
            Objects.isNull(product.getAccessEntitlements())
                ? new AccessEntitlement()
                : product.getAccessEntitlements()));

    product.setCredentials(buildCredential(productDTO.getCredentials()));
    product.setOptions(buildOptions(productDTO.getOptions()));
    product.setAdditionalLinks((buildAdditionalLinks(productDTO.getAdditionalLinks())));

    log.debug("build_product: DML Action {} {}", dmlAction, LOGGER_END);
    return product;
  }

  /**
   * Build additional link entity from dto
   *
   * @param additionalLinkDTOList
   * @return
   */
  private List<AdditionalLink> buildAdditionalLinks(List<AdditionalLinkDTO> additionalLinkDTOList) {
    log.debug("build_additional_links {}", LOGGER_START);
    if (CollectionUtils.isEmpty(additionalLinkDTOList)) {
      return new ArrayList<>();
    }
    List<AdditionalLink> additionalLinksList = new ArrayList<>();
    additionalLinkDTOList.forEach(
        dto -> {
          AdditionalLink entity = AdditionalLink.builder().build();
          BeanUtils.copyProperties(dto, entity);
          additionalLinksList.add(entity);
        });
    log.debug("build_additional_links {}", LOGGER_END);
    return additionalLinksList;
  }

  /**
   * checks whether the data type matches
   *
   * @param object incoming data content
   * @param clazz expected datatype
   * @return true or false
   */
  public boolean isValidDataType(Object object, Class<?> clazz) {
    return clazz.isInstance(object);
  }

  /**
   * Check if persona is authorized to get/update product. For admin, no need to check for
   * environment
   *
   * @param isUpdate for PATCH/PUT call, this flag will be true
   * @return true or false
   */
  public boolean isAuthorized(Product product, boolean isUpdate) {
    boolean result = false;

    log.info("has_permission {}{}", "isUpdate=", isUpdate);

    if (CommonUtil.hasInternalAdminToken()) {
      log.info("has_permission {}", "Internal Admin");
      result = true;
    } else if (CommonUtil.isBizDevUser()) {
      log.info("has_permission {}", "Biz Dev user");
      result = true;
    } else if (CommonUtil.hasPartnerToken()
        && CommonUtil.isPartnerAuthorized(product.getPartnerId())
        && isEnvironmentAuthorized(product.getEnvironment())) {

      log.info("has_permission {}", "valid partner");
      result = true;
    } else if (!isUpdate
        && accessEntitlementUtil.isTenantAuthorized(product.getAccessEntitlements())
        && isEnvironmentAuthorized(product.getEnvironment())) {

      log.info("has_permission {}", "valid lender or S2S jwt");
      result = true;
    }

    log.info("has_permission {}{}", "return value=", result);
    return result;
  }

  /**
   * checks whether the product and instance environment are authorized Sandbox instance should be
   * able to place txn for approved product
   *
   * @param productEnvironment product environment
   * @return true or false
   */
  private boolean isEnvironmentAuthorized(String productEnvironment) {

    boolean result = false;
    String jwtTokenEnv = CommonUtil.getProductEnvironment();

    log.info(
        "is_environment_authorized product_env={}, jwt_token_env={}",
        productEnvironment,
        jwtTokenEnv);

    if (jwtTokenEnv.equals(productEnvironment)
        || (EnvironmentType.sandbox.name().equals(jwtTokenEnv)
            && EnvironmentType.prod.name().equals(productEnvironment))) {
      result = true;
    }

    log.info("is_environment_authorized result={}", result);
    return result;
  }

  /**
   * Check is have Data Entitlements
   *
   * @param entitlementDTO entitlementDTO
   * @return boolean if have data entitlement
   */
  public boolean hasDataEntitlements(EntitlementDTO entitlementDTO) {
    return (Objects.nonNull(entitlementDTO) && Objects.nonNull(entitlementDTO.getData()));
  }

  /**
   * Check is have Access Entitlements
   *
   * @param entitlementDTO entitlementDTO
   * @return boolean if have access entitlement
   */
  public boolean hasAccessEntitlements(EntitlementDTO entitlementDTO) {
    return (Objects.nonNull(entitlementDTO) && Objects.nonNull(entitlementDTO.getAccess()));
  }

  /**
   * Check if credentials contain duplicate element.
   *
   * @param credentials
   * @return
   */
  public boolean containDuplicateCredential(List<CredentialDTO> credentials) {

    Set<String> duplicateIdSet = new HashSet<>();

    return credentials.stream()
        .anyMatch(
            credentialDTO ->
                !StringUtils.isEmpty(credentialDTO.getId())
                    && !duplicateIdSet.add(credentialDTO.getId()));
  }

  /**
   * Builds feature entity data from dto
   *
   * @param featureDTO incoming feature dto payload
   * @return feature data entity
   */
  private Feature buildFeature(FeatureDTO featureDTO) {
    Feature feature = new Feature();
    if (Objects.nonNull(featureDTO)) {
      BeanUtils.copyProperties(featureDTO, feature);
      feature.setUx(buildUx(featureDTO.getUx()));
    }

    return feature;
  }

  /**
   * Build Ux from UxDTO
   *
   * @param uxDTO
   * @return
   */
  private Ux buildUx(UxDTO uxDTO) {
    if (Objects.isNull(uxDTO)) {
      return null;
    }
    Ux ux = new Ux();
    BeanUtils.copyProperties(uxDTO, ux);
    return ux;
  }

  /**
   * Builds feature dto from entity data
   *
   * @param feature incoming feature dto payload
   * @return feature data entity
   */
  private FeatureDTO buildFeatureDTO(Feature feature) {
    FeatureDTO featureDTO = new FeatureDTO();
    if (Objects.nonNull(feature)) {
      BeanUtils.copyProperties(feature, featureDTO);
      featureDTO.setUx(buildUxDTO(feature.getUx()));
    }

    return featureDTO;
  }

  /**
   * Builds uxDTO from entity data
   *
   * @param ux
   * @return
   */
  private UxDTO buildUxDTO(Ux ux) {
    if (Objects.isNull(ux)) {
      return null;
    }
    UxDTO uxDTO = new UxDTO();
    BeanUtils.copyProperties(ux, uxDTO);
    return uxDTO;
  }

  /**
   * Builds credential data list from credentialDto
   *
   * @param credentials source credentialDto
   * @return credential data list
   */
  private List<Credential> buildCredential(List<CredentialDTO> credentials) {
    List<Credential> targetCredential = null;

    if (Objects.nonNull(credentials)) {

      targetCredential = new ArrayList<>();

      for (CredentialDTO credentialDTO : credentials) {
        Credential credential = new Credential();
        credentialDTO.setScope(
            Objects.isNull(credentialDTO.getScope()) ? AccessScope.user : credentialDTO.getScope());
        BeanUtils.copyProperties(credentialDTO, credential);
        targetCredential.add(credential);
      }
    }

    return targetCredential;
  }

  /**
   * Builds options data list from incoming product payload
   *
   * @param options source optionsDTO
   * @return options schema list
   */
  private List<Options> buildOptions(List<OptionsDTO> options) {
    log.debug("build_options {}", LOGGER_START);
    List<Options> targetOptions = null;

    if (Objects.nonNull(options)) {

      targetOptions = new ArrayList<>();

      for (OptionsDTO optionsDTO : options) {
        // Inject "additionalProperties": false for any incoming option schemas
        optionsDTO.getSchema().put(ADDITIONAL_PROPERTIES, false);

        Options optionsData = new Options();
        BeanUtils.copyProperties(optionsDTO, optionsData);
        targetOptions.add(optionsData);
      }
    }
    log.debug("build_options {}", LOGGER_END);
    return targetOptions;
  }

  /**
   * Builds credential dto list from credential data
   *
   * @param credentials data list
   * @return CredentialDTO list
   */
  private List<CredentialDTO> buildCredentialDTO(List<Credential> credentials) {

    List<CredentialDTO> credentialDTOS = new ArrayList<>();

    for (Credential credential : credentials) {
      CredentialDTO credentialDTO = new CredentialDTO();
      BeanUtils.copyProperties(credential, credentialDTO);
      credentialDTOS.add(credentialDTO);
    }

    return credentialDTOS;
  }

  /**
   * Builds options dto list from options db data
   *
   * @param optionsList data list
   * @return OptionsDTO list
   */
  private List<OptionsDTO> buildOptionsDTO(List<Options> optionsList) {

    List<OptionsDTO> optionsDTOS = new ArrayList<>();

    for (Options options : optionsList) {
      OptionsDTO optionsDTO = new OptionsDTO();
      BeanUtils.copyProperties(options, optionsDTO);
      optionsDTOS.add(optionsDTO);
    }

    return optionsDTOS;
  }

  /**
   * Build AccessEntitlement
   *
   * @param sourceAccessEntitlementDTO Source AccessEntitlementDTO
   * @param targetAccessEntitlement Target AccessEntitlement entity object
   * @return Target AccessEntitlement
   */
  private AccessEntitlement buildAccessEntitlement(
      AccessEntitlementDTO sourceAccessEntitlementDTO, AccessEntitlement targetAccessEntitlement) {

    if (Objects.isNull(sourceAccessEntitlementDTO)) {
      targetAccessEntitlement =
          AccessEntitlement.builder().allow(EMPTY_REGEX).deny(EMPTY_REGEX).build();
    } else {
      if (!CollectionUtils.isEmpty(sourceAccessEntitlementDTO.getAllow())) {
        targetAccessEntitlement.setAllow(
            accessEntitlementUtil.getPatternString(sourceAccessEntitlementDTO.getAllow()));
      } else {
        targetAccessEntitlement.setAllow(EMPTY_REGEX);
      }

      if (!CollectionUtils.isEmpty(sourceAccessEntitlementDTO.getDeny())) {
        targetAccessEntitlement.setDeny(
            accessEntitlementUtil.getPatternString(sourceAccessEntitlementDTO.getDeny()));
      } else {
        targetAccessEntitlement.setDeny(EMPTY_REGEX);
      }
    }
    return targetAccessEntitlement;
  }

  /**
   * Build AccessEntitlementDTO
   *
   * @param sourceAccessEntitlement Source AccessEntitlement
   * @return Target AccessEntitlementDTO
   */
  private AccessEntitlementDTO buildAccessEntitlementDTO(
      AccessEntitlement sourceAccessEntitlement) {
    AccessEntitlementDTO targetAccessEntitlementDTO = AccessEntitlementDTO.builder().build();
    targetAccessEntitlementDTO.setAllow(
        accessEntitlementUtil.getPatternList(sourceAccessEntitlement.getAllow()));

    targetAccessEntitlementDTO.setDeny(
        accessEntitlementUtil.getPatternList(sourceAccessEntitlement.getDeny()));

    return targetAccessEntitlementDTO;
  }

  /**
   * Get Webhook subscription details
   *
   * @param subscriptionIds Webhook subscription ids
   * @return Collection of webhook subscription details
   */
  private List<WebhookDTO> buildWebhookDTO(List<String> subscriptionIds) {

    List<WebhookDTO> webhooks = null;
    if (!CollectionUtils.isEmpty(subscriptionIds)) {
      webhooks = new ArrayList<>();
      for (String subscriptionId : subscriptionIds) {
        webhooks.add(subscriptionService.getSubscription(subscriptionId));
      }
    }

    return webhooks;
  }

  /**
   * Build Manifest
   *
   * @param manifestDto Manifest DTO details
   * @param manifest Manifest Entity
   * @return Manifest entity
   */
  private Manifest buildManifest(ManifestDTO manifestDto, Manifest manifest) {
    BeanUtils.copyProperties(manifestDto, manifest);

    List<TransactionEntitlement> transactionEntitlements;
    if (!CollectionUtils.isEmpty(manifestDto.getTransactions())) {
      transactionEntitlements = buildTransactionEntitlements(manifestDto);
    } else {
      transactionEntitlements = new ArrayList<>();
    }

    manifest.setTransactions(transactionEntitlements);
    manifest.setOrigin(buildManifestFields(manifestDto.getOrigin()));
    manifest.setFindings(buildFindingEntity(manifestDto.getFindings()));
    manifest.setServiceEvents(buildServiceEventsEntity(manifestDto.getServiceEvents()));

    return manifest;
  }

  /**
   * Builds FindingDTO
   *
   * @param findingDTO Finding DTO
   * @return Finding entity
   */
  private Finding buildFindingEntity(FindingDTO findingDTO) {

    if (Objects.isNull(findingDTO)) {
      return null;
    }

    log.debug("build_finding {}", LOGGER_START);
    Finding finding = Finding.builder().build();
    BeanUtils.copyProperties(findingDTO, finding);

    finding.setTypes(new ArrayList<>());
    for (FindingTypeDTO findingTypeDTO : findingDTO.getTypes()) {
      log.debug("finding_type code={}", findingTypeDTO.getCode());
      FindingType findingType = new FindingType();
      BeanUtils.copyProperties(findingTypeDTO, findingType);
      finding.getTypes().add(findingType);
    }

    log.info("findings size={}", finding.getTypes().size());
    log.debug("build_finding {}", LOGGER_END);
    return finding;
  }

  /**
   * Builds ServiceEvents
   *
   * @param serviceEventDTO ServiceEvent DTO
   * @return SterviceEvent entity
   */
  private ServiceEvent buildServiceEventsEntity(ServiceEventDTO serviceEventDTO) {
    log.debug("build_serviceEvents {}", LOGGER_START);
    if (Objects.isNull(serviceEventDTO)) {
      return null;
    }

    ServiceEvent serviceEvent = ServiceEvent.builder().build();
    serviceEvent.setTypes(new ArrayList<>());
    for (ServiceEventTypeDTO serviceEventTypeDTO : serviceEventDTO.getTypes()) {
      log.debug("service_events_type code={}", serviceEventTypeDTO.getCode());
      ServiceType serviceType = new ServiceType();
      BeanUtils.copyProperties(serviceEventTypeDTO, serviceType);

      serviceType.setSenders(
          serviceEventTypeDTO.getSenders().stream()
              .map(SenderType::find)
              .collect(Collectors.toList()));

      serviceType.setType(
          serviceEventTypeDTO.getType().stream()
              .map(ServiceEventType::find)
              .collect(Collectors.toList()));

      serviceEvent.getTypes().add(serviceType);
    }

    log.info("serviceEvents size={}", serviceEvent.getTypes().size());
    log.debug("build_serviceEvents {}", LOGGER_END);
    return serviceEvent;
  }

  /**
   * Builds FindingDTO
   *
   * @param finding Finding
   * @return Finding DTO
   */
  private FindingDTO buildFindingDTO(Finding finding) {
    if (Objects.isNull(finding)) {
      return null;
    }

    log.debug("build_finding_dto {}", LOGGER_START);
    FindingDTO findingDto = FindingDTO.builder().build();
    BeanUtils.copyProperties(finding, findingDto);

    findingDto.setTypes(new ArrayList<>());
    for (FindingType findingType : finding.getTypes()) {
      log.debug("finding_type code={}", findingType.getCode());
      FindingTypeDTO findingTypeDTO = new FindingTypeDTO();
      BeanUtils.copyProperties(findingType, findingTypeDTO);
      findingDto.getTypes().add(findingTypeDTO);
    }

    log.info("finding_dto size={}", findingDto.getTypes().size());
    log.debug("build_finding_dto {}", LOGGER_END);
    return findingDto;
  }

  /**
   * Builds ServiceEventDTO
   *
   * @param serviceEvent ServiceEvent
   * @return ServiceEvent DTO
   */
  private ServiceEventDTO buildServiceEventsDTO(ServiceEvent serviceEvent) {
    log.debug("build_serviceEvents_dto {}", LOGGER_START);
    if (Objects.isNull(serviceEvent)) {
      return null;
    }

    ServiceEventDTO serviceEventDTO = ServiceEventDTO.builder().types(new ArrayList<>()).build();
    for (ServiceType serviceType : serviceEvent.getTypes()) {
      log.debug("service_event_type code={}", serviceType.getCode());

      ServiceEventTypeDTO serviceEventTypeDTO =
          ServiceEventTypeDTO.builder()
              .code(serviceType.getCode())
              .name(serviceType.getName())
              .senders(
                  serviceType.getSenders().stream()
                      .map(SenderType::toString)
                      .collect(Collectors.toList()))
              .type(
                  serviceType.getType().stream()
                      .map(ServiceEventType::toString)
                      .collect(Collectors.toList()))
              .build();
      serviceEventDTO.getTypes().add(serviceEventTypeDTO);
    }

    log.info("serviceEvent_dto size={}", serviceEventDTO.getTypes().size());
    log.debug("build_serviceEvents_dto {}", LOGGER_END);
    return serviceEventDTO;
  }

  /**
   * Build TransactionEntitlement entities
   *
   * @param manifestDTO ManifestDTO with TransactionEntitlementsDTO list
   * @return List of TransactionEntitlement entities
   */
  private List<TransactionEntitlement> buildTransactionEntitlements(ManifestDTO manifestDTO) {
    List<TransactionEntitlement> transactionEntitlements = new ArrayList<>();
    for (TransactionEntitlementDTO transactionEntitlementDTO : manifestDTO.getTransactions()) {
      TransactionEntitlement transactionEntitlement =
          TransactionEntitlement.builder()
              .request(buildManifestFields(transactionEntitlementDTO.getRequest()))
              .response(buildResponseManifestFields(transactionEntitlementDTO.getResponse()))
              .build();
      BeanUtils.copyProperties(transactionEntitlementDTO, transactionEntitlement);
      transactionEntitlements.add(transactionEntitlement);
    }
    return transactionEntitlements;
  }

  /**
   * this method builds Origin and Request manifest
   *
   * @param manifestDTO manifestDTO
   * @return manifest entity object
   */
  private ManifestRequest buildManifestFields(PlmAttributeDef manifestDTO) {
    ManifestRequest manifest = ManifestRequest.builder().build();
    manifest.setFields(new ArrayList<>());

    if (Objects.nonNull(manifestDTO)) {

      manifest.setResources(manifestDTO.getResources());

      for (FieldDTO fieldDto : manifestDTO.getFields()) {
        Field field = new Field();
        BeanUtils.copyProperties(fieldDto, field);
        manifest.getFields().add(field);
      }

      if (!CollectionUtils.isEmpty(manifestDTO.getConditions())) {
        List<Condition> conditionList = new ArrayList<>();
        for (ConditionDTO conditionDTO : manifestDTO.getConditions()) {
          Condition condition = new Condition();
          BeanUtils.copyProperties(conditionDTO, condition);
          conditionList.add(condition);
        }
        manifest.setConditions(conditionList);
      }
      if (!CollectionUtils.isEmpty(manifestDTO.getExports())) {
        List<Export> exportList = new ArrayList<>();
        for (ExportDTO exportDTO : manifestDTO.getExports()) {
          Export export = new Export();
          BeanUtils.copyProperties(exportDTO, export);
          exportList.add(export);
        }
        manifest.setExports(exportList);
      }
    }
    return manifest;
  }

  /**
   * this method builds Response manifest with results
   *
   * @param manifestDTO manifestDTO
   * @return manifest entity object
   */
  private ManifestRequest buildResponseManifestFields(PlmAttributeDef manifestDTO) {
    ManifestRequest manifest = buildManifestFields(manifestDTO);

    if (Objects.nonNull(manifestDTO)) {
      manifest.setResults(buildResponseResults(manifestDTO.getResults()));
      manifest.setResources(manifestDTO.getResources());
      manifest.setSkipDefaultResourceContainerCreation(
          manifestDTO.getSkipDefaultResourceContainerCreation());
    }

    return manifest;
  }

  /**
   * this method build Response results object which includes action/formats
   *
   * @param results object from response entitlement
   * @return result entity object
   */
  private List<Result> buildResponseResults(List<ResultDTO> results) {
    List<Result> resultList = null;
    if (!CollectionUtils.isEmpty(results)) {
      resultList =
          results.stream()
              .map(
                  resultDTO -> {
                    Result result = new Result();
                    BeanUtils.copyProperties(resultDTO, result);

                    return result;
                  })
              .collect(Collectors.toList());
    }
    return resultList;
  }

  /**
   * this method build Response resultsDTO which includes action/formats
   *
   * @param results object from response entitlement
   * @return resultDTO list
   */
  private List<ResultDTO> buildResponseResultsDTO(List<Result> results) {
    List<ResultDTO> resultDTOList = null;
    if (!CollectionUtils.isEmpty(results)) {
      resultDTOList =
          results.stream()
              .map(
                  result -> {
                    ResultDTO resultDTO = new ResultDTO();
                    BeanUtils.copyProperties(result, resultDTO);
                    return resultDTO;
                  })
              .collect(Collectors.toList());
    }

    log.info("ResultDTO list size={}", resultDTOList == null ? 0 : resultDTOList.size());
    return resultDTOList;
  }

  /**
   * Builds ManifestDTO
   *
   * @param manifest Manifest entity
   * @param manifestDto Manifest DTO
   * @return Manifest DTO
   */
  private ManifestDTO buildManifestDTO(Manifest manifest, ManifestDTO manifestDto) {
    manifestDto = Objects.isNull(manifestDto) ? new ManifestDTO() : manifestDto;

    BeanUtils.copyProperties(manifest, manifestDto);
    manifestDto.setTransactions(buildTransactionEntitlementDTO(manifest));
    manifestDto.setOrigin(buildManifestDTOFields(manifest.getOrigin()));
    manifestDto.setFindings(buildFindingDTO(manifest.getFindings()));
    manifestDto.setServiceEvents(buildServiceEventsDTO(manifest.getServiceEvents()));
    manifestDto.setCreated(CommonUtil.getISODateAsString(manifest.getCreated()));
    manifestDto.setUpdated(CommonUtil.getISODateAsString(manifest.getUpdated()));
    return manifestDto;
  }

  /**
   * builds list of Transaction entitlements DTO object
   *
   * @param manifest manifest entity object
   * @return list of Transaction entitlements entity object
   */
  private List<TransactionEntitlementDTO> buildTransactionEntitlementDTO(Manifest manifest) {
    List<TransactionEntitlementDTO> transactionEntitlementDTOs = new ArrayList<>();
    for (TransactionEntitlement transactionEntitlement : manifest.getTransactions()) {
      TransactionEntitlementDTO transactionEntitlementDTO =
          TransactionEntitlementDTO.builder()
              .requestTypes(transactionEntitlement.getRequestTypes())
              .request(buildManifestDTOFields(transactionEntitlement.getRequest()))
              .response(buildResponseManifestDTOFields(transactionEntitlement.getResponse()))
              .build();

      BeanUtils.copyProperties(manifest, transactionEntitlementDTO);
      transactionEntitlementDTOs.add(transactionEntitlementDTO);
    }
    return transactionEntitlementDTOs;
  }

  /**
   * this method builds manifest fields for Origin, Request and Response
   *
   * @param manifest manifest entity object
   * @return manifest fields
   */
  private PlmAttributeDef buildManifestDTOFields(ManifestRequest manifest) {
    PlmAttributeDef manifestDto = PlmAttributeDef.builder().build();
    manifestDto.setFields(new ArrayList<>());

    if (Objects.nonNull(manifest)) {

      manifestDto.setResources(manifest.getResources());

      for (Field field : manifest.getFields()) {
        FieldDTO fieldDto = new FieldDTO();
        BeanUtils.copyProperties(field, fieldDto);
        manifestDto.getFields().add(fieldDto);
      }

      if (!CollectionUtils.isEmpty(manifest.getConditions())) {
        List<ConditionDTO> conditionDTOList = new ArrayList<>();
        for (Condition condition : manifest.getConditions()) {
          ConditionDTO conditionDTO = ConditionDTO.builder().build();
          BeanUtils.copyProperties(condition, conditionDTO);
          conditionDTOList.add(conditionDTO);
        }
        manifestDto.setConditions(conditionDTOList);
      }
      if (!CollectionUtils.isEmpty(manifest.getExports())) {
        List<ExportDTO> exportDTOList = new ArrayList<>();
        for (Export export : manifest.getExports()) {
          ExportDTO exportDto = new ExportDTO();
          BeanUtils.copyProperties(export, exportDto);
          exportDTOList.add(exportDto);
        }
        manifestDto.setExports(exportDTOList);
      }
    }
    return manifestDto;
  }

  /**
   * this method builds Response filed of manifestDTO
   *
   * @param manifest manifest entity object
   * @return manifest response fields
   */
  private PlmAttributeDef buildResponseManifestDTOFields(ManifestRequest manifest) {

    PlmAttributeDef manifestDto = buildManifestDTOFields(manifest);

    if (Objects.nonNull(manifest)) {
      manifestDto.setResults(buildResponseResultsDTO(manifest.getResults()));
      manifestDto.setResources(manifest.getResources());
      manifestDto.setSkipDefaultResourceContainerCreation(
          manifest.getSkipDefaultResourceContainerCreation());
    }
    return manifestDto;
  }

  /**
   * Get the expected Environment type
   *
   * @param productStatusType Product Status
   * @return Return environment type need to be set
   */
  private EnvironmentType getEnvironment(ProductStatusType productStatusType) {

    EnvironmentType environmentType = null;
    if (ProductStatusType.approved.equals(productStatusType)) {
      environmentType = EnvironmentType.prod;
    } else {
      environmentType = EnvironmentType.sandbox;
    }
    return environmentType;
  }

  /**
   * Check if it's a manifest view
   *
   * @param view View parameter
   * @return Return if view param is a manifest view
   */
  public boolean isManifestView(String view) {
    return MANIFEST_VIEWS.contains(EntityView.findByCode(view));
  }

  /**
   * Check if it's a webhook view
   *
   * @param view View parameter
   * @return Return if view param is a webhook view
   */
  private boolean isWebhookView(String view) {

    EntityView entityView = EntityView.findByCode(view);
    return EntityView.WEBHOOK.equals(entityView) || EntityView.COMPLETE.equals(entityView);
  }

  /**
   * Check if it's a billingRules view
   *
   * @param view View parameter
   * @return Return if view param is a billingRules view
   */
  private boolean isBillingRulesView(String view) {

    EntityView entityView = EntityView.findByCode(view);
    return EntityView.BILLINGRULES.equals(entityView) || EntityView.COMPLETE.equals(entityView);
  }

  /**
   * Check if it's a options view
   *
   * @param view View parameter
   * @return Return if view param is a options view
   */
  private boolean isOptionsView(String view) {

    EntityView entityView = EntityView.findByCode(view);
    return EntityView.OPTIONS.equals(entityView) || EntityView.COMPLETE.equals(entityView);
  }

  /**
   * Extract TagType query param in the product search request and put them into tags map
   *
   * @param productSearchMap Product search request map
   * @return Query Tag map
   */
  public Map<String, List<String>> buildProductTagQueryParams(
      Map<String, Object> productSearchMap) {

    Map<String, List<String>> tagQueryParams = new HashMap<>();
    if (!CollectionUtils.isEmpty(productSearchMap)) {
      productSearchMap.forEach(
          (paramName, paramValue) -> {
            if (TagType.getTagTypes().contains(paramName.toLowerCase())
                && !StringUtils.isEmpty(paramValue)) {
              List<String> categoriesValue =
                  Arrays.asList(paramValue.toString().split("\\s*,\\s*"));
              if (paramName.equalsIgnoreCase(TagType.CATEGORIES.toString())) {
                categoriesValue.replaceAll(String::toUpperCase);
              }
              tagQueryParams.put(paramName.toLowerCase(), categoriesValue);
            }
          });
    }

    return tagQueryParams;
  }

  /**
   * Pre-process product search attributes before performing a search
   *
   * @param productSearch Product Search Attributes to be initialized
   */
  public void buildProductSearchRequest(ProductSearchDTO productSearch) {

    Map<String, Object> productSearchMap = objectMapper.convertValue(productSearch, Map.class);

    if (StringUtils.isEmpty(productSearch.getLimit())) {
      productSearch.setLimit(String.valueOf(defaultQueryLimit));
    }

    if (CommonUtil.hasPartnerToken()) {
      productSearch.setPartnerId(IdentityContext.get().getClientId());
      productSearchMap.put(PARTNER_ID, productSearch.getPartnerId());
    }

    if (CommonUtil.hasAnyLenderToken()) {
      productSearch.setTenant(IdentityContext.get().getRealm());
      productSearchMap.put(TENANT, productSearch.getTenant());
    }

    String environment = CommonUtil.getProductEnvironment();
    productSearch.setEnvironment(
        PARTNER_TEST_ENVIRONMENT.equalsIgnoreCase(environment)
            ? EnvironmentType.sandbox.name()
            : environment);
    productSearchMap.put(ENVIRONMENT, productSearch.getEnvironment());

    if (Objects.nonNull(productSearch.getExtensions())) {
      productSearchMap.put(EXTENSION_LIMIT, productSearch.getExtensions());
      productSearchMap.remove(EXTENSIONS);
    }

    if (Objects.nonNull(productSearch.getRequestType())) {
      productSearchMap.put(REQUEST_TYPES_ATTRIBUTE, Arrays.asList(productSearch.getRequestType()));
      productSearchMap.remove(REQUEST_TYPE);
    }

    productSearch.setWhereClause(productSearchMap);
    productSearch.setTagCriteria(buildProductSearchTagCriteria(productSearch.getTags()));
    productSearch.setRequestTypesCriteria(
        buildProductSearchRequestTypesCriteria(productSearch.getRequestType()));
    productSearch.setOrderByClause(buildProductSearchOrderByClause(productSearch.getSort()));
  }

  /**
   * Build Product Page Request
   *
   * @param productSearch ProductSearch input
   * @return Product Page Request
   */
  public ProductPageRequest buildProductSearchPageRequest(ProductSearchDTO productSearch) {
    int start =
        (StringUtils.isEmpty(productSearch.getStart())
            ? 0
            : Integer.parseInt(productSearch.getStart()));
    int limit =
        (StringUtils.isEmpty(productSearch.getLimit())
            ? 0
            : Integer.parseInt(productSearch.getLimit()));

    Direction sortDirection =
        (!CollectionUtils.isEmpty(productSearch.getOrderByClause())
                && productSearch.getOrderByClause().contains("-" + LISTING_NAME))
            ? Direction.DESC
            : Direction.ASC;

    return new ProductPageRequest(start, limit, Sort.by(sortDirection, LISTING_NAME));
  }

  /**
   * Build Product Tag Where Criteria
   *
   * @param tags Product Tags
   * @return Tag Criteria String
   */
  private String buildProductSearchTagCriteria(Map<String, List<String>> tags) {

    if (!CollectionUtils.isEmpty(tags)) {
      return LEFT_CURLY_BRACKET + buildTags(tags) + RIGHT_CURLY_BRACKET;
    }

    return null;
  }

  /**
   * Build Product requestTypes Where Criteria
   *
   * @param requestType Product requestTypes
   * @return requestType Criteria String
   */
  private String buildProductSearchRequestTypesCriteria(String requestType) {
    if (Objects.nonNull(requestType)) {
      return JSON_PATH_LEFT_BRACKET
          + ESCAPE_QUOTE
          + requestType
          + ESCAPE_QUOTE
          + JSON_PATH_RIGHT_BRACKET;
    }
    return null;
  }

  private String buildTags(Map<String, List<String>> tags) {
    return tags.entrySet().stream()
        .map(
            entry ->
                ESCAPE_QUOTE
                    + entry.getKey()
                    + ESCAPE_QUOTE
                    + COLON
                    + JSON_PATH_LEFT_BRACKET
                    + entry.getValue().stream()
                        .map(value -> ESCAPE_QUOTE + value + ESCAPE_QUOTE)
                        .collect(joining(","))
                    + JSON_PATH_RIGHT_BRACKET)
        .collect(joining(","));
  }

  /**
   * Build Order by list
   *
   * @param sort Comma-separated Product Search Sort Attributes
   * @return List of order by attributes
   */
  private List<String> buildProductSearchOrderByClause(String sort) {
    return (Objects.nonNull(sort) ? Arrays.asList(sort.split(",")) : null);
  }

  /**
   * returns list of environments
   *
   * @param jwtEnvironment product environment
   * @return environment as string
   */
  public List<String> getEnvironments(String jwtEnvironment) {

    List<String> environments = new ArrayList<>();
    environments.add(jwtEnvironment);

    log.info("get_environment jwt_token_env={}", jwtEnvironment);

    if (!EnvironmentType.prod.name().equals(jwtEnvironment)) {
      environments.add(EnvironmentType.prod.name());
    }

    return environments;
  }

  /**
   * This method is to build Product Query View in case of view variable is null/empty
   *
   * @param productQueryDTO productQueryDTO
   * @param view default view
   */
  public void buildProductQueryView(ProductQueryDTO productQueryDTO, String view) {

    if (StringUtils.isEmpty(productQueryDTO.getView())) {
      productQueryDTO.setView(view);
    }

    log.info("build_product_query_view view={}", productQueryDTO.getView());
  }

  /**
   * This method is to build Product List Query View
   *
   * @param productListQueryDTO productQueryDTO
   */
  public void buildProductListQueryView(ProductListQueryDTO productListQueryDTO) {

    if (ObjectUtils.isEmpty(productListQueryDTO.getView())) {
      productListQueryDTO.setView(VIEW_CREDENTIAL);
    }

    log.info("build_product_list_query_view view={}", productListQueryDTO.getView());
  }

  public void sanitizeRequestType(ProductSearchDTO productSearch) {
    if (!ObjectUtils.isEmpty(productSearch.getRequestType())) {
      productSearch.setRequestType(productSearch.getRequestType().replaceAll("^\"|\"$", ""));
    }
  }

  /**
   * build kafka product payload
   *
   * @param product product
   * @return kafka product payload
   */
  public Payload buildKafkaProductPayload(Product product) {
    log.debug("build_kafka_product_payload {}", LOGGER_START);

    Payload payload =
        Payload.builder()
            .partnerId(product.getPartnerId())
            .name(product.getName())
            .listingName(product.getListingName())
            .environment(product.getEnvironment())
            .status(product.getStatus())
            .extensionLimit(product.getExtensionLimit())
            .integrationType(product.getIntegrationType())
            .requestTypes(product.getRequestTypes())
            .tags(product.getTags())
            .feature(buildFeatureDTO(product.getFeature()))
            .vendorPlatform(EPC2)
            .build();

    if (Objects.nonNull(product.getCredentials())) {
      payload.setCredentials(buildCredentialDTO(product.getCredentials()));
    }

    payload.setEntitlements(buildEntitlementDTO(product));

    log.debug("build_kafka_product_payload {}", LOGGER_END);
    return payload;
  }

  /**
   * build entitlement object from product entity
   *
   * @param product product
   * @return EntitlementDTO
   */
  private EntitlementDTO buildEntitlementDTO(Product product) {
    log.debug("build_entitlement_dto {}", LOGGER_START);

    ManifestDTO manifestDto = null;

    if (Objects.nonNull(product.getManifest())) {
      manifestDto = new ManifestDTO();

      BeanUtils.copyProperties(
          product.getManifest(), manifestDto, CREATED_BY, UPDATED_BY, CREATED_DATE, UPDATED_DATE);

      updateOrigin(manifestDto, product.getManifest());
      manifestDto.setTransactions(buildTransactionEntitlementsForKafka(product.getManifest()));
    }

    AccessEntitlementDTO accessEntitlementDTO = null;
    if (Objects.nonNull(product.getAccessEntitlements())) {
      accessEntitlementDTO = buildAccessEntitlementDTO(product.getAccessEntitlements());
    }

    log.debug("build_entitlement_dto {}", LOGGER_END);
    return EntitlementDTO.builder().access(accessEntitlementDTO).data(manifestDto).build();
  }

  /**
   * update origin to kafka payload
   *
   * @param manifestDto manifestDto
   * @param manifest manifest
   */
  private void updateOrigin(ManifestDTO manifestDto, Manifest manifest) {
    log.debug("update_origin {}", LOGGER_START);

    if (Objects.nonNull(manifest.getOrigin())) {
      manifestDto.setOrigin(new PlmAttributeDef());

      manifestDto.getOrigin().setHasFields(hasFields(manifest.getOrigin().getFields()));
    }

    log.debug("update_origin {}", LOGGER_END);
  }

  /**
   * build list of transaction Entitlements for kafka
   *
   * @param manifest manifest
   * @return List<TransactionEntitlementDTO>
   */
  private List<TransactionEntitlementDTO> buildTransactionEntitlementsForKafka(Manifest manifest) {
    log.debug("build_transaction_entitlement_for_kafka {}", LOGGER_START);

    List<TransactionEntitlementDTO> transactionEntitlementDTOs = new ArrayList<>();

    if (CollectionUtils.isEmpty(manifest.getTransactions())) {
      return transactionEntitlementDTOs;
    }

    for (TransactionEntitlement transactionEntitlement : manifest.getTransactions()) {

      TransactionEntitlementDTO transactionEntitlementDTO =
          TransactionEntitlementDTO.builder()
              .requestTypes(transactionEntitlement.getRequestTypes())
              .build();

      // build request object
      PlmAttributeDef request = buildRequest(transactionEntitlement);
      transactionEntitlementDTO.setRequest(request);

      // build response object
      PlmAttributeDef response = buildResponse(transactionEntitlement);
      transactionEntitlementDTO.setResponse(response);

      transactionEntitlementDTOs.add(transactionEntitlementDTO);
    }

    log.debug("build_transaction_entitlement_for_kafka {}", LOGGER_END);
    return transactionEntitlementDTOs;
  }

  /**
   * build response object from transactionEntitlement
   *
   * @param transactionEntitlement transactionEntitlement
   * @return request object
   */
  private PlmAttributeDef buildRequest(TransactionEntitlement transactionEntitlement) {
    log.debug("build_request {}", LOGGER_START);

    if (Objects.isNull(transactionEntitlement.getRequest())) {
      return null;
    }

    PlmAttributeDef request = new PlmAttributeDef();

    // set request exports
    if (!CollectionUtils.isEmpty(transactionEntitlement.getRequest().getExports())) {
      List<ExportDTO> exportDTOList = new ArrayList<>();
      for (Export export : transactionEntitlement.getRequest().getExports()) {
        ExportDTO exportDto = new ExportDTO();
        BeanUtils.copyProperties(export, exportDto);
        exportDTOList.add(exportDto);
      }
      request.setExports(exportDTOList);
    }

    request.setHasFields(hasFields(transactionEntitlement.getRequest().getFields()));
    request.setResources(transactionEntitlement.getRequest().getResources());
    log.debug("build_request {}", LOGGER_END);
    return request;
  }

  /**
   * build fields dto for request or response
   *
   * @param incomingFields incomingFields
   * @return true/false based on fields:[]
   */
  private Boolean hasFields(List<Field> incomingFields) {

    return !CollectionUtils.isEmpty(incomingFields);
  }

  /**
   * build response object from transactionEntitlement
   *
   * @param transactionEntitlement transactionEntitlement
   * @return response object
   */
  private PlmAttributeDef buildResponse(TransactionEntitlement transactionEntitlement) {
    log.debug("build_response {}", LOGGER_START);

    if (Objects.isNull(transactionEntitlement.getResponse())) {
      return null;
    }

    PlmAttributeDef response = new PlmAttributeDef();

    // set response resources
    response.setResources(transactionEntitlement.getResponse().getResources());

    // setting results
    List<Result> results = transactionEntitlement.getResponse().getResults();
    if (!CollectionUtils.isEmpty(results)) {
      response.setResults(buildResults(results));
    }

    response.setHasFields(hasFields(transactionEntitlement.getResponse().getFields()));

    log.debug("build_response {}", LOGGER_END);
    return response;
  }

  /**
   * This method will build the results object under response entitlements
   *
   * @param results
   * @return
   */
  private List<ResultDTO> buildResults(List<Result> results) {
    List<ResultDTO> resultDTOS = new ArrayList<>();

    for (Result result : results) {
      ResultDTO resultDTO =
          ResultDTO.builder().action(result.getAction()).formats(result.getFormats()).build();
      resultDTOS.add(resultDTO);
    }

    return resultDTOS;
  }
}
