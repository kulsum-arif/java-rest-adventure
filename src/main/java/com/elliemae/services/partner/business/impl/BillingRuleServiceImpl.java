package com.synkrato.services.partner.business.impl;

import static com.synkrato.services.epc.common.EpcCommonConstants.LOGGER_END;
import static com.synkrato.services.epc.common.EpcCommonConstants.LOGGER_ID;
import static com.synkrato.services.epc.common.EpcCommonConstants.LOGGER_START;

import com.synkrato.components.microservice.exception.DataNotFoundException;
import com.synkrato.services.epc.common.dto.BillingRuleDTO;
import com.synkrato.services.epc.common.dto.ProductDTO;
import com.synkrato.services.epc.common.dto.enums.BillingRuleStatus;
import com.synkrato.services.epc.common.error.EpcRuntimeException;
import com.synkrato.services.epc.common.util.CommonUtil;
import com.synkrato.services.epc.common.util.MessageUtil;
import com.synkrato.services.partner.business.BillingRuleChangeLogService;
import com.synkrato.services.partner.business.BillingRuleService;
import com.synkrato.services.partner.business.ProductService;
import com.synkrato.services.partner.data.BillingRule;
import com.synkrato.services.partner.data.BillingRuleChangeLog;
import com.synkrato.services.partner.data.jpa.BillingRuleRepository;
import com.synkrato.services.partner.data.jpa.ProductRepository;
import com.synkrato.services.partner.enums.EntityView;
import com.synkrato.services.partner.util.BillingRuleUpdateUtil;
import com.synkrato.services.partner.util.BillingRuleUtil;
import com.synkrato.services.partner.util.ProductUtil;
import com.synkrato.services.partner.validators.BillingRuleUpdateValidator;
import com.synkrato.services.partner.validators.BillingRuleValidator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;

@Slf4j
@Service
@Transactional
public class BillingRuleServiceImpl implements BillingRuleService {

  @Autowired private MessageUtil messageUtil;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private BillingRuleUtil billingRuleUtil;
  @Autowired private BillingRuleUpdateUtil billingRuleUpdateUtil;
  @Autowired private ProductUtil productUtil;
  @Autowired private BillingRuleChangeLogService billingRuleChangeLogService;
  @Autowired private BillingRuleRepository billingRuleRepository;
  @Autowired private ProductRepository productRepository;
  @Autowired private BillingRuleValidator billingRuleValidator;
  @Autowired private BillingRuleUpdateValidator billingRuleUpdateValidator;
  @Autowired private ProductService productService;
  private static final String CREATED = "CREATED";
  private static final String BILLING_RULES_RULE_CREATED = "Billing rules created.";
  private static final String UPDATED = "UPDATED";
  private static final String BILLING_RULES_RULE_UPDATED = "Billing rules updated.";

  /**
   * This method will create a new billing rule for a given product
   *
   * @param billingRuleDTO incoming billing rule dto
   * @return returns newly created billingRule from the DB
   */
  @Override
  public BillingRuleDTO create(BillingRuleDTO billingRuleDTO) {
    log.debug("create_billing_rule {}", LOGGER_START);

    log.info("billing rule being created for product_id={}", billingRuleDTO.getProductId());

    // this will check if the productId exists and is valid.
    ProductDTO productDTO =
        productService.findById(billingRuleDTO.getProductId(), EntityView.ID.name());

    // check if the incoming request has any status other than draft
    if (Objects.nonNull(billingRuleDTO.getStatus())
        && !BillingRuleStatus.DRAFT.equals(billingRuleDTO.getStatus())) {

      throw new EpcRuntimeException(
          HttpStatus.BAD_REQUEST,
          messageUtil.getMessage(
              "synkrato.services.partner.product.billing.status.create.invalid"));

    } else {
      // Any billing rule when it is created will always be in a DRAFT state
      billingRuleDTO.setStatus(BillingRuleStatus.DRAFT);
    }

    // This method will check if the requestTypes specified in the transformation are valid or not
    billingRuleValidator.validateBillingRuleForProduct(productDTO, billingRuleDTO);

    // validates if there is any existing billing rule for the given product.
    // this is to make sure there is at most one billingRule in DRAFT state for the given product.
    validateExistingDraft(billingRuleDTO.getProductId());

    BillingRule billingRule = billingRuleUtil.buildBillingRule(billingRuleDTO);

    billingRule = billingRuleRepository.saveAndFlush(billingRule);

    BillingRuleChangeLog changeLog =
        BillingRuleChangeLog.builder()
            .changesetId(billingRule.getVersionNumber())
            .billingRuleId(billingRule.getId())
            .content(
                objectMapper.convertValue(
                    billingRuleDTO, new TypeReference<Map<String, Object>>() {}))
            .operation(CREATED)
            .comments(BILLING_RULES_RULE_CREATED)
            .build();

    billingRuleChangeLogService.createChangeLog(changeLog);

    BillingRuleDTO updatedBillingRule = billingRuleUtil.buildBillingRuleDTO(billingRule);

    log.debug("create_billing_rule {}", LOGGER_END);
    return updatedBillingRule;
  }

  /**
   * This method will update the billing rule
   *
   * @param productId productId for which the billing rule is being updated
   * @param id billing rule id for record which is being updated
   * @param updateBillingRuleMap incoming billing rule map which is to be updated
   * @return returns an updated view of the billing record
   */
  @Override
  public BillingRuleDTO update(
      String productId, String id, Map<String, Object> updateBillingRuleMap)
      throws MethodArgumentNotValidException {
    log.debug("update_billing_rule {}", LOGGER_START);

    log.info("billing rule being updated for product_id={}, billingRule_id={}", productId, id);

    // this will check if the productId exists and is valid.
    ProductDTO productDTO = productService.findById(productId, EntityView.ID.name());

    // Only an admin can change the status to approved/rejected
    billingRuleUpdateValidator.validateIfAuthorizedToUpdate(productDTO);

    // checks if billing rule with given id exists and is valid
    BillingRule billingRule = findEntity(productId, id);

    // A billing rule can only be modified if it is in DRAFT state
    if (Objects.isNull(billingRule)
        || !BillingRuleStatus.DRAFT.getValue().equalsIgnoreCase(billingRule.getStatus())) {
      throw new EpcRuntimeException(
          HttpStatus.BAD_REQUEST,
          messageUtil.getMessage(
              "synkrato.services.partner.product.billing.status.invalid.action"));
    }

    BillingRuleDTO billingRuleDTO = billingRuleUtil.buildBillingRuleDTO(billingRule);

    log.info("billing rule found for given product");

    // performing pre merge validations
    billingRuleUpdateUtil.preMergeValidate(updateBillingRuleMap, billingRuleDTO);

    // performing merge operation here
    BillingRuleDTO mergedBillingRuleDto =
        billingRuleUpdateUtil.merge(updateBillingRuleMap, billingRuleDTO);

    // performing post merge validation
    billingRuleUpdateUtil.postMergeValidate(mergedBillingRuleDto);

    // This method will check if the requestTypes specified in the transformation are valid or not
    billingRuleValidator.validateBillingRuleForProduct(productDTO, billingRuleDTO);

    billingRuleUtil.buildBillingRule(billingRule, mergedBillingRuleDto);

    int previousVersionNumber = billingRule.getVersionNumber();

    BillingRule updatedBillingRule = billingRuleRepository.saveAndFlush(billingRule);

    log.info(
        "currentbilling_rule_version={}, updated_billing_rule_version={}",
        previousVersionNumber,
        updatedBillingRule.getVersionNumber());

    // this method will create a change log for the current action if the version has changed
    if (previousVersionNumber != updatedBillingRule.getVersionNumber()) {

      // We deprecate all the existing billing rules if the current one being modified is Approved
      deprecateExistingBillingRules(mergedBillingRuleDto);

      BillingRuleChangeLog changeLog =
          BillingRuleChangeLog.builder()
              .changesetId(updatedBillingRule.getVersionNumber())
              .billingRuleId(updatedBillingRule.getId())
              .comments(BILLING_RULES_RULE_UPDATED)
              .operation(UPDATED)
              .content(
                  objectMapper.convertValue(
                      billingRuleDTO, new TypeReference<Map<String, Object>>() {}))
              .build();

      billingRuleChangeLogService.createChangeLog(changeLog);
    }

    log.debug("update_billing_rule {}", LOGGER_END);

    return billingRuleUtil.buildBillingRuleDTO(billingRule);
  }

  /**
   * This method will deprecate all existing billing rules for the given product
   *
   * @param mergedBillingRuleDTO the billing rule that is being updated
   */
  private void deprecateExistingBillingRules(BillingRuleDTO mergedBillingRuleDTO) {
    log.debug("deprecate_existing_billing_rules {}", LOGGER_START);

    if (!BillingRuleStatus.APPROVED.equals(mergedBillingRuleDTO.getStatus())) {
      log.info(
          "Not deprecating existing billing rules since status={}",
          mergedBillingRuleDTO.getStatus().getValue());
      return;
    }

    // fetching existing billing rules for the given product
    List<BillingRule> billingRules =
        billingRuleRepository.findByProductId(
            CommonUtil.getUUIDFromString(mergedBillingRuleDTO.getProductId()));

    // deprecating all the billing rules that are not deprecated.
    if (!CollectionUtils.isEmpty(billingRules)) {

      for (BillingRule billingRule : billingRules) {

        if (!billingRule.getId().toString().equals(mergedBillingRuleDTO.getId())
            && !BillingRuleStatus.DEPRECATED.getValue().equalsIgnoreCase(billingRule.getStatus())
            && !BillingRuleStatus.REJECTED.getValue().equalsIgnoreCase(billingRule.getStatus())) {

          log.info(
              "Deprecating billing_rule_id={}, current_status={}",
              billingRule.getId(),
              billingRule.getStatus());
          billingRule.setStatus(BillingRuleStatus.DEPRECATED.getValue());
        }
      }
    }

    log.debug("deprecate_existing_billing_rules {}", LOGGER_END);
  }

  /**
   * This method will return billing rules for a given product
   *
   * @param productId identifier for the given product
   * @return list of billing rules for a given product
   */
  @Override
  public List<BillingRuleDTO> findBillingRules(String productId) {
    log.debug("find_billing_rules_by_product_id {}", LOGGER_START);

    List<BillingRuleDTO> billingRuleDTOS = null;

    List<BillingRule> billingRules =
        billingRuleRepository.findByProductId(CommonUtil.getUUIDFromString(productId));

    if (!CollectionUtils.isEmpty(billingRules)) {
      log.info(
          "billing rules found. product_id={}, billing_rule_count={}",
          productId,
          billingRules.size());
      billingRuleDTOS = billingRuleUtil.buildBillingRulesDTO(billingRules);
    }

    log.debug("find_billing_rules_by_product_id {}", LOGGER_END);
    return billingRuleDTOS;
  }

  /**
   * Find billing rule by billing rule id and productId
   *
   * @param productId productId
   * @param id billingRule id
   * @return BillingRule entity
   */
  private BillingRule findEntity(String productId, String id) {
    log.debug("find_billing_rule_entity {}", LOGGER_END);

    BillingRule billingRule = null;
    try {

      Optional<BillingRule> billingRuleOptional =
          billingRuleRepository.findOptionalByProductIdAndId(
              CommonUtil.getUUIDFromString(productId), CommonUtil.getUUIDFromString(id));

      if (!billingRuleOptional.isPresent()) {
        log.error("find_billing_rule_entity not found {}{}", LOGGER_ID, id);
        throw new DataNotFoundException("BillingRule", id);
      }

      billingRule = billingRuleOptional.get();

    } catch (IllegalArgumentException iae) {
      log.error("Invalid Billing rule UUID message={} id={}", iae.getMessage(), id, iae);
    }

    // we throw an exception if the billing rule was not found or if the billingRuleId is invalid
    if (Objects.isNull(billingRule)) {
      throw new DataNotFoundException("BillingRule", id);
    }

    log.debug("find_billing_rule_entity {}", LOGGER_END);
    return billingRule;
  }

  /**
   * This method will get existing billing rules and check if a DRAFT version already exists. There
   * should only be at most one billing rule in DRAFT and one billing rule in APPROVED state
   *
   * @param productId productId of the incoming billing rule
   */
  private void validateExistingDraft(String productId) {
    log.debug("validate_existing_draft {}", LOGGER_START);

    List<BillingRuleDTO> existingBillingRules = findBillingRules(productId);

    if (!CollectionUtils.isEmpty(existingBillingRules)
        && existingBillingRules.stream()
            .anyMatch(billingRule -> BillingRuleStatus.DRAFT.equals(billingRule.getStatus()))) {

      log.info("exiting billing rule found in 'DRAFT' state. hence failing.");
      throw new EpcRuntimeException(
          HttpStatus.CONFLICT,
          messageUtil.getMessage("synkrato.services.partner.product.billing.status.invalid"));
    }

    log.debug("validate_existing_draft {}", LOGGER_END);
  }
}
