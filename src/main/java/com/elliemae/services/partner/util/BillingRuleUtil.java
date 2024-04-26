package com.synkrato.services.partner.util;

import static com.synkrato.services.epc.common.EpcCommonConstants.LOGGER_END;
import static com.synkrato.services.epc.common.EpcCommonConstants.LOGGER_START;
import static com.synkrato.services.partner.PartnerServiceConstants.DEFAULT_SCHEMA_VERSION;

import com.synkrato.services.epc.common.dto.BillingRuleDTO;
import com.synkrato.services.epc.common.dto.TransformationDTO;
import com.synkrato.services.epc.common.dto.enums.BillingRuleStatus;
import com.synkrato.services.epc.common.util.CommonUtil;
import com.synkrato.services.partner.data.BillingRule;
import com.synkrato.services.partner.data.Transformation;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

@Component
@Slf4j
public class BillingRuleUtil {

  private static final String BUILD_BILLING_RULE_LOG = "build_billing_rule {}";

  /**
   * This method will build the billingRule entity from the billingRuleDTO received in the request
   *
   * @param source incoming billingRule DTO
   * @return returns the billingRule entity that will be updated in the db
   */
  public BillingRule buildBillingRule(BillingRuleDTO source) {
    log.debug(BUILD_BILLING_RULE_LOG, LOGGER_START);

    BillingRule billingRule = BillingRule.builder().build();

    BeanUtils.copyProperties(source, billingRule);
    billingRule.setSchemaVersion(DEFAULT_SCHEMA_VERSION);
    billingRule.setProductId(CommonUtil.getUUIDFromString(source.getProductId()));
    billingRule.setStatus(source.getStatus().getValue());
    billingRule.setTransformations(buildTransformations(source.getTransformations()));

    log.debug(BUILD_BILLING_RULE_LOG, LOGGER_END);
    return billingRule;
  }

  /**
   * This method will build the billingRule entity from the billingRuleDTO received in the request
   *
   * @param billingRule existing billing rule
   * @param source incoming billingRule DTO
   * @return returns the billingRule entity that will be updated in the db
   */
  public BillingRule buildBillingRule(BillingRule billingRule, BillingRuleDTO source) {
    log.debug(BUILD_BILLING_RULE_LOG, LOGGER_START);

    BeanUtils.copyProperties(source, billingRule);

    // Schema version will only be set if the incoming version is empty
    if (ObjectUtils.isEmpty(billingRule.getSchemaVersion())) {
      billingRule.setSchemaVersion(DEFAULT_SCHEMA_VERSION);
    }
    billingRule.setProductId(CommonUtil.getUUIDFromString(source.getProductId()));
    billingRule.setStatus(source.getStatus().getValue());
    billingRule.setTransformations(buildTransformations(source.getTransformations()));

    log.debug(BUILD_BILLING_RULE_LOG, LOGGER_END);
    return billingRule;
  }

  /**
   * This method will build the billingRule entity from the billingRuleDTO received in the request
   *
   * @param source incoming billingRule DTO
   * @return returns the billingRule entity that will be updated in the db
   */
  public BillingRuleDTO buildBillingRuleDTO(BillingRule source) {
    log.debug("build_billing_rule_dto {}", LOGGER_START);

    BillingRuleDTO billingRuleDTO = BillingRuleDTO.builder().build();

    BeanUtils.copyProperties(source, billingRuleDTO);
    billingRuleDTO.setId(source.getId().toString());
    billingRuleDTO.setStatus(BillingRuleStatus.valueOf(source.getStatus().toUpperCase()));

    billingRuleDTO.setCreated(source.getCreated());
    billingRuleDTO.setUpdated(source.getUpdated());

    if (Objects.nonNull(source.getProduct())) {
      billingRuleDTO.setProductId(source.getProduct().getId().toString());
    }

    billingRuleDTO.setTransformations(buildTransformationDTO(source.getTransformations()));

    log.debug("build_billing_rule_dto {}", LOGGER_END);
    return billingRuleDTO;
  }

  /**
   * This method will build the transformation entity
   *
   * @param transformations incoming transformations DTO
   * @return returns transformations entity
   */
  private List<Transformation> buildTransformations(List<TransformationDTO> transformations) {
    log.debug("build_transformations {}", LOGGER_START);
    List<Transformation> targetTransformation = null;

    if (!CollectionUtils.isEmpty(transformations)) {

      targetTransformation = new ArrayList<>();

      for (TransformationDTO transformationDTO : transformations) {
        Transformation transformation = Transformation.builder().build();

        BeanUtils.copyProperties(transformationDTO, transformation);
        targetTransformation.add(transformation);
      }
    }

    log.debug("build_transformations {}", LOGGER_END);
    return targetTransformation;
  }

  /**
   * This method will build the TransformationDTO
   *
   * @param transformations transformations entity fetched from the DB
   * @return returns TransformationDTO
   */
  private List<TransformationDTO> buildTransformationDTO(List<Transformation> transformations) {
    log.debug("build_transformation_dto {}", LOGGER_START);
    List<TransformationDTO> targetTransformationDTO = null;

    if (!CollectionUtils.isEmpty(transformations)) {

      targetTransformationDTO = new ArrayList<>();

      for (Transformation Transformation : transformations) {
        TransformationDTO transformationDTO = TransformationDTO.builder().build();

        BeanUtils.copyProperties(Transformation, transformationDTO);
        targetTransformationDTO.add(transformationDTO);
      }
    }

    log.debug("build_transformation_dto {}", LOGGER_END);
    return targetTransformationDTO;
  }

  /**
   * This method will convert billingRule entities to billingRule DTOs
   *
   * @param billingRules billingRule entity
   * @return returns billingRuleDTOs
   */
  public List<BillingRuleDTO> buildBillingRulesDTO(List<BillingRule> billingRules) {
    log.debug("build_billing_rule_dto_list {}", LOGGER_START);

    List<BillingRuleDTO> billingRuleDTOS = null;

    if (!CollectionUtils.isEmpty(billingRules)) {
      billingRuleDTOS = new ArrayList<>();
      for (BillingRule billingRule : billingRules) {
        BillingRuleDTO billingRuleDTO = buildBillingRuleDTO(billingRule);

        billingRuleDTOS.add(billingRuleDTO);
      }
    }

    log.debug("build_billing_rule_dto_list {}", LOGGER_END);
    return billingRuleDTOS;
  }
}
