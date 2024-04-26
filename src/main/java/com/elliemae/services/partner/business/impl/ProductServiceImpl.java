package com.synkrato.services.partner.business.impl;

import static com.synkrato.services.epc.common.EpcCommonConstants.CLIENT_ID;
import static com.synkrato.services.epc.common.EpcCommonConstants.DML_INSERT;
import static com.synkrato.services.epc.common.EpcCommonConstants.DML_UPDATE_PATCH;
import static com.synkrato.services.epc.common.EpcCommonConstants.ID_VIEW;
import static com.synkrato.services.epc.common.EpcCommonConstants.LOGGER_END;
import static com.synkrato.services.epc.common.EpcCommonConstants.LOGGER_ID;
import static com.synkrato.services.epc.common.EpcCommonConstants.LOGGER_START;
import static com.synkrato.services.epc.common.EpcCommonConstants.WEBHOOK;
import static com.synkrato.services.epc.common.kafka.EventConstants.ENVELOPE_VERSION;
import static com.synkrato.services.epc.common.kafka.EventConstants.EVENT_CATEGORY_TYPE;
import static com.synkrato.services.epc.common.kafka.EventConstants.EVENT_SOURCE;
import static com.synkrato.services.epc.common.kafka.EventConstants.PARTNER_PRODUCT_TENANT;
import static com.synkrato.services.epc.common.kafka.EventConstants.PRODUCT_KAFKA_PAYLOAD_VERSION;
import static com.synkrato.services.partner.PartnerServiceConstants.CATEGORIES;
import static com.synkrato.services.partner.PartnerServiceConstants.ENDPOINT;
import static com.synkrato.services.partner.PartnerServiceConstants.EVENTS;
import static com.synkrato.services.partner.PartnerServiceConstants.INSTANCE_ID;
import static com.synkrato.services.partner.PartnerServiceConstants.RESOURCE;
import static com.synkrato.services.partner.PartnerServiceConstants.SIGNING_KEY;
import static com.synkrato.services.partner.PartnerServiceConstants.SUBSCRIPTION_ID;
import static com.synkrato.services.partner.PartnerServiceConstants.TAG_ATTRIBUTE;
import static java.util.stream.Collectors.toList;

import com.synkrato.components.microservice.exception.DataNotFoundException;
import com.synkrato.components.microservice.web.filter.CorrelationIdFilter;
import com.synkrato.services.epc.common.dto.ProductDTO;
import com.synkrato.services.epc.common.dto.WebhookDTO;
import com.synkrato.services.epc.common.dto.enums.EnvironmentType;
import com.synkrato.services.epc.common.dto.enums.ProductStatusType;
import com.synkrato.services.epc.common.dto.enums.TagType;
import com.synkrato.services.epc.common.error.EpcRuntimeException;
import com.synkrato.services.epc.common.kafka.KafkaUtil;
import com.synkrato.services.epc.common.kafka.ProductEventType;
import com.synkrato.services.epc.common.kafka.ProductKafkaEvent;
import com.synkrato.services.epc.common.logging.LogExecutionTime;
import com.synkrato.services.epc.common.util.CommonUtil;
import com.synkrato.services.epc.common.util.MessageUtil;
import com.synkrato.services.partner.business.ProductService;
import com.synkrato.services.partner.business.webhook.SubscriptionService;
import com.synkrato.services.partner.data.Product;
import com.synkrato.services.partner.data.jpa.CustomProductRepository;
import com.synkrato.services.partner.data.jpa.ProductRepository;
import com.synkrato.services.partner.data.jpa.domain.ProductPageRequest;
import com.synkrato.services.partner.data.jpa.domain.ProductSpecification;
import com.synkrato.services.partner.dto.ProductSearchDTO;
import com.synkrato.services.partner.enums.EntityView;
import com.synkrato.services.partner.util.ProductUpdateUtil;
import com.synkrato.services.partner.util.ProductUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;

@Slf4j
@Service
@Transactional
public class ProductServiceImpl implements ProductService {

  @Autowired private MessageUtil messageUtil;
  @Autowired private ProductUtil productUtil;
  @Autowired private ProductUpdateUtil productUpdateUtil;
  @Autowired private ProductRepository productRepository;
  @Autowired private CustomProductRepository customProductRepository;
  @Autowired private SubscriptionService subscriptionService;
  @Autowired private ProductSpecification productSpecification;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private KafkaUtil kafkaUtil;

  @Value("${synkrato.kafka.topic}")
  private String kafkaTopic;

  /**
   * Create Product
   *
   * @param productDTO Product details
   * @param view Json view param
   * @return productDTO New product
   */
  @Override
  public ProductDTO create(ProductDTO productDTO, String view) {
    log.debug("Create product {}", LOGGER_START);

    // Initialize implicit values for product
    productDTO.setEnvironment(EnvironmentType.sandbox);
    productDTO.setStatus(ProductStatusType.development);
    productDTO.setPartnerId(ProductUtil.getPartnerId(productDTO));
    Product product = null;

    try {

      if (!CollectionUtils.isEmpty(productDTO.getTags())
          && !CollectionUtils.isEmpty(productDTO.getTags().get(TagType.CATEGORIES))) {
        // Convert categories to uppercase, if exists
        productDTO
            .getTags()
            .replace(
                TagType.CATEGORIES,
                ProductUtil.convertCategoriesToUpperCase(
                    productDTO.getTags().get(TagType.CATEGORIES)));
      }

      // Build Product Entity
      product = productUtil.buildProduct(new Product(), productDTO, DML_INSERT);

      // process webhook list, if applicable
      if (!CollectionUtils.isEmpty(productDTO.getWebhooks())) {
        List<Map<String, Object>> createSubscriptionsList = new ArrayList<>();

        // list of webhook subscriptions to create
        List<WebhookDTO> createWebhookList =
            productDTO.getWebhooks().stream()
                .filter(webhookDTO -> StringUtils.isEmpty(webhookDTO.getSubscriptionId()))
                .collect(Collectors.toList());

        // create the subscription list
        for (WebhookDTO webhookDTO : createWebhookList) {
          createSubscriptionsList.add(
              buildSubscriptionPayload(
                  productDTO.getPartnerId(), productDTO.getName(), webhookDTO));
        }

        // create subscriptions
        List<WebhookDTO> webhookDTOList =
            subscriptionService.createSubscription(createSubscriptionsList);

        if (!CollectionUtils.isEmpty(webhookDTOList)) {
          productDTO.setWebhooks(webhookDTOList);
          product.setWebhookSubscriptions(
              webhookDTOList.stream().map(WebhookDTO::getSubscriptionId).collect(toList()));
        }
      }

      // store product information
      product = productRepository.save(product);

      // publish event to kafka
      publishKafkaEvent(product, DML_INSERT);

      productDTO = productUtil.buildProductDTO(product, productDTO, false, view);

    } catch (Exception e) {

      log.error("Create Product failed with {}", e.getMessage(), e);

      // delete webhook subscriptions, if created
      if (Objects.nonNull(product) && !CollectionUtils.isEmpty(product.getWebhookSubscriptions())) {
        subscriptionService.deleteSubscriptions(product.getWebhookSubscriptions());
      }

      throw e;
    }

    log.info("create product by {}{}", LOGGER_ID, product.getId());
    log.debug("Create Product {}", LOGGER_END);
    return productDTO;
  }

  /**
   * Update Product
   *
   * @param updateProductMap Input product payload map
   * @return productDTO Updated product
   */
  @Override
  @LogExecutionTime
  public ProductDTO update(String id, Map<String, Object> updateProductMap, String view)
      throws MethodArgumentNotValidException {
    log.debug("update Product {}", LOGGER_START);

    Product product = findEntity(id, true);
    ProductDTO productDTO =
        productUtil.buildProductDTO(product, new ProductDTO(), true, EntityView.COMPLETE.name());

    if (!productUtil.isAuthorized(product, true)) {
      throw new EpcRuntimeException(
          HttpStatus.FORBIDDEN, messageUtil.getMessage("synkrato.services.epc.forbidden.error"));
    }

    productUpdateUtil.premergeValidate(updateProductMap, productDTO);
    // Convert categories to uppercase
    if (updateProductMap.containsKey(TAG_ATTRIBUTE)
        && updateProductMap.get(TAG_ATTRIBUTE) instanceof Map) {
      Map<String, List<String>> tags =
          (Map<String, List<String>>) updateProductMap.get(TAG_ATTRIBUTE);
      tags.replace(CATEGORIES, ProductUtil.convertCategoriesToUpperCase(tags.get(CATEGORIES)));
    }
    ProductDTO mergedProductDto = productUpdateUtil.merge(updateProductMap, productDTO);
    productUpdateUtil.postmergeValidate(mergedProductDto);

    if (hasWebhook(updateProductMap)) {
      product = updateSubscription(product, mergedProductDto);
    }

    applyStatusWorkflow(mergedProductDto);

    productUtil.buildProduct(product, mergedProductDto, DML_UPDATE_PATCH);
    product = productRepository.saveAndFlush(product);

    // publish event to kafka
    publishKafkaEvent(product, DML_UPDATE_PATCH);

    log.info("update product by id={}", product.getId());
    log.debug("Update Product {}", LOGGER_END);
    return productUtil.buildProductDTO(product, mergedProductDto, false, view);
  }

  /**
   * Publish event to kafka
   *
   * @param product product object
   * @param dmlAction insert/update
   */
  private void publishKafkaEvent(Product product, String dmlAction) {
    log.debug("publish_kafka_event {}", LOGGER_START);

    ProductKafkaEvent kafkaEvent = new ProductKafkaEvent();

    kafkaEvent.setId(UUID.randomUUID().toString());
    kafkaEvent.setCorrelationId(MDC.get(CorrelationIdFilter.KEY_CORRELATION_ID));
    kafkaEvent.setCategory(EVENT_CATEGORY_TYPE);
    kafkaEvent.setCreatedDate(CommonUtil.getCurrentISODateAsString());
    kafkaEvent.setEnvelopeVersion(ENVELOPE_VERSION);
    kafkaEvent.setPayloadVersion(PRODUCT_KAFKA_PAYLOAD_VERSION);
    kafkaEvent.setSource(EVENT_SOURCE);
    kafkaEvent.setEntityId(product.getId().toString());
    kafkaEvent.setTenant(PARTNER_PRODUCT_TENANT + product.getPartnerId());

    kafkaEvent.setType(
        DML_UPDATE_PATCH.equals(dmlAction)
            ? ProductEventType.TYPE_PRODUCT_UPDATED.getCode()
            : ProductEventType.TYPE_PRODUCT_CREATED.getCode());

    kafkaEvent.setPayload(productUtil.buildKafkaProductPayload(product));

    kafkaUtil.sendEvent(kafkaTopic, product.getId().toString(), kafkaEvent);
    log.info(
        "published kafka notification for product_id={} action={}", product.getId(), dmlAction);

    log.debug("publish_kafka_event {}", LOGGER_END);
  }

  /**
   * Set allow list to empty list when internalAdmin is deprecating a product.
   *
   * @param mergedProductDto
   */
  private void applyStatusWorkflow(ProductDTO mergedProductDto) {
    switch (mergedProductDto.getStatus()) {
      case deprecated:
        mergedProductDto.getEntitlements().getAccess().setAllow(new ArrayList<>());
        break;
    }
  }

  /**
   * determines whether webhook exist in the incoming payload or not
   *
   * @param updateProductMap incoming product payload
   * @return true or false
   */
  private boolean hasWebhook(Map<String, Object> updateProductMap) {
    return (Objects.nonNull(updateProductMap.get(WEBHOOK))
        && !CollectionUtils.isEmpty((List) updateProductMap.get(WEBHOOK)));
  }

  /**
   * @param productSearch Search params
   * @return List of products
   */
  @Override
  public List<ProductDTO> findAll(ProductSearchDTO productSearch, String view) {
    log.debug("find_all products {}", LOGGER_START);

    productUtil.sanitizeRequestType(productSearch);
    List<ProductDTO> productList = new ArrayList<>();

    if (Objects.isNull(productSearch.getRequestType())
        || !ObjectUtils.isEmpty(productSearch.getRequestType())) {

      productUtil.buildProductSearchRequest(productSearch);
      List<Product> productEntityList = findProducts(productSearch);

      if (!CollectionUtils.isEmpty(productEntityList)) {
        productEntityList.forEach(
            productEntity ->
                productList.add(
                    productUtil.buildProductDTO(productEntity, new ProductDTO(), true, view)));
      }

      log.info("No. of products found={}", productList.size());
    }
    log.debug("find_all products {}", LOGGER_END);
    return productList;
  }

  /**
   * Find products by partner id and product name without enforcing permission - internal use only
   *
   * @param partnerId Partner id
   * @param name Product name
   * @return List of products
   */
  @Override
  public List<ProductDTO> findByPartnerIdAndName(String partnerId, String name) {
    log.debug("find_by_partner_id_and_name {}", LOGGER_START);

    List<ProductDTO> productList = new ArrayList<>();

    List<Product> productEntityList = productRepository.findByPartnerIdAndName(partnerId, name);

    if (!CollectionUtils.isEmpty(productEntityList)) {
      productEntityList.forEach(
          productEntity ->
              productList.add(
                  productUtil.buildProductDTO(productEntity, new ProductDTO(), true, ID_VIEW)));
    }

    log.info("size={}", productList.size());
    log.debug("find_by_partner_id_and_name {}", LOGGER_END);

    return productList;
  }

  /**
   * @param id Product id
   * @return producDTO Product details
   */
  @Override
  public ProductDTO findById(String id, String view) {
    log.debug("find_product_by_id {}", LOGGER_START);
    log.info("find product by id={}", id);

    Product product = findEntity(id, false);
    ProductDTO productDTO = productUtil.buildProductDTO(product, new ProductDTO(), true, view);
    log.debug("find_product_by_id {}", LOGGER_END);
    return productDTO;
  }

  /**
   * Find product by product id
   *
   * @param id product id
   * @param isUpdate flag to check whether jwt identity has update permission or not
   * @return Product entity
   */
  private Product findEntity(String id, boolean isUpdate) {
    Product product = null;
    try {
      Optional<Product> productOptional =
          productRepository.findById(CommonUtil.getUUIDFromString(id));
      if (productOptional.isPresent()) {
        product = productOptional.get();
      }
    } catch (IllegalArgumentException iae) {
      log.error("Invalid Product message={} id={}", iae.getMessage(), id, iae);
    }

    if (Objects.isNull(product)) {
      log.error("find_product_by_id not found {}{}", LOGGER_ID, id);
      throw new DataNotFoundException("Product", id);
    }

    if (!productUtil.isAuthorized(product, isUpdate)) {
      throw new EpcRuntimeException(
          HttpStatus.FORBIDDEN, messageUtil.getMessage("synkrato.services.epc.forbidden.error"));
    }

    return product;
  }

  /**
   * Find all products
   *
   * @param productSearch search params
   * @return Product(s)
   */
  private List<Product> findProducts(ProductSearchDTO productSearch) {
    log.debug("find_Products_By_Criteria {}", LOGGER_START);

    productSpecification.setWhereClauseFields(productSearch.getWhereClause());
    productSpecification.setTagCriteria(productSearch.getTagCriteria());
    productSpecification.setRequestTypesCriteria(productSearch.getRequestTypesCriteria());

    ProductPageRequest productPageRequest =
        productUtil.buildProductSearchPageRequest(productSearch);

    Page<Product> productResult =
        customProductRepository.findAll(productSpecification, productPageRequest);

    List<Product> productList = productResult.getContent();
    productSearch.setTotalProductCount(productResult.getTotalElements());

    log.info("product_list_size={}", productList.size());
    log.debug("find_Products_By_Criteria {}", LOGGER_END);

    return productList;
  }

  /**
   * update webhook(s)
   *
   * @param product, productDTO
   * @return Product
   */
  @LogExecutionTime
  private Product updateSubscription(Product product, ProductDTO productDTO)
      throws EpcRuntimeException {
    log.debug("update_subscription {}", LOGGER_START);
    List<String> createSubscriptionIds = new ArrayList<>();
    List<String> updateSubscriptionIds = new ArrayList<>();
    List<String> deleteSubscriptionsIds = null;
    List<Map<String, Object>> createSubscriptionsList = new ArrayList<>();
    List<Map<String, Object>> updateSubscriptionsList = new ArrayList<>();

    for (WebhookDTO webhookDTO : productDTO.getWebhooks()) {
      if (!StringUtils.isEmpty(webhookDTO.getSubscriptionId())) {
        updateSubscriptionsList.add(
            buildSubscriptionPayload(product.getPartnerId(), product.getName(), webhookDTO));
        updateSubscriptionIds.add(webhookDTO.getSubscriptionId());
      } else {
        createSubscriptionsList.add(
            buildSubscriptionPayload(product.getPartnerId(), product.getName(), webhookDTO));
      }
    }
    // Determine the difference between existing subscriptions to delete unwanted
    if (!CollectionUtils.isEmpty(product.getWebhookSubscriptions())) {
      deleteSubscriptionsIds =
          product.getWebhookSubscriptions().stream()
              .filter(webhookSubscriptions -> !updateSubscriptionIds.contains(webhookSubscriptions))
              .collect(toList());
    }
    try {
      List<WebhookDTO> webhookDTOList =
          subscriptionService.createSubscription(createSubscriptionsList);

      if (!CollectionUtils.isEmpty(webhookDTOList)) {
        createSubscriptionIds =
            webhookDTOList.stream().map(WebhookDTO::getSubscriptionId).collect(toList());
        productDTO.getWebhooks().removeIf(e -> StringUtils.isEmpty(e.getSubscriptionId()));
        productDTO.getWebhooks().addAll(webhookDTOList);
      }

      if (!CollectionUtils.isEmpty(createSubscriptionIds)) {
        if (CollectionUtils.isEmpty(product.getWebhookSubscriptions())) {
          product.setWebhookSubscriptions(createSubscriptionIds);
        } else {
          product.getWebhookSubscriptions().addAll(createSubscriptionIds);
        }
      }
      // update existing subscriptions if exist
      subscriptionService.updateSubscription(updateSubscriptionsList);

      // Delete Subscriptions based on delete list
      if (!CollectionUtils.isEmpty(deleteSubscriptionsIds)) {
        subscriptionService.deleteSubscriptions(deleteSubscriptionsIds);
        product.getWebhookSubscriptions().removeAll(deleteSubscriptionsIds);
      }
    } catch (EpcRuntimeException e) {
      // rollback to the existing subscriptions, if any error in try block
      if (!CollectionUtils.isEmpty(createSubscriptionIds)) {
        subscriptionService.deleteSubscriptions(createSubscriptionIds);
      }
      throw e;
    }
    log.info("update subscription id={}", product.getId());
    log.debug("update_subscription {}", LOGGER_END);
    return product;
  }

  /**
   * Build Webhook subscription payload
   *
   * @param partnerId Partner id
   * @param productName Product name
   * @param webhookDTO Webhook details
   * @return Webhook subscription payload
   */
  private Map<String, Object> buildSubscriptionPayload(
      String partnerId, String productName, WebhookDTO webhookDTO) {

    Map<String, Object> payload = new HashMap<>();
    payload.put(CLIENT_ID, partnerId);
    payload.put(INSTANCE_ID, productName);
    payload.put(EVENTS, webhookDTO.getEvents());
    if (!StringUtils.isEmpty(webhookDTO.getSigningkey())) {
      payload.put(SIGNING_KEY, webhookDTO.getSigningkey());
    }
    if (!StringUtils.isEmpty(webhookDTO.getSubscriptionId())) {
      payload.put(SUBSCRIPTION_ID, webhookDTO.getSubscriptionId());
    }
    payload.put(ENDPOINT, webhookDTO.getUrl());
    payload.put(RESOURCE, webhookDTO.getResource());

    return payload;
  }

  /**
   * Validate the incoming regex against the database. This is done because the all java regex
   * patterns are not valid for Postgres. Hence, the safest way to validate is to do it against the
   * database itself. It executes a simple select query that be found on the interface for more
   * details.
   */
  @Override
  public boolean validateRegex(String regex) {
    return productRepository.validatePosixRegex(regex);
  }
}
