package com.synkrato.services.partner.business.impl;

import static com.synkrato.services.epc.common.EpcCommonConstants.LOGGER_END;
import static com.synkrato.services.epc.common.EpcCommonConstants.LOGGER_START;

import com.synkrato.components.microservice.exception.DataNotFoundException;
import com.synkrato.services.epc.common.dto.BillingRuleChangeLogDTO;
import com.synkrato.services.epc.common.util.CommonUtil;
import com.synkrato.services.partner.business.BillingRuleChangeLogService;
import com.synkrato.services.partner.data.BillingRuleChangeLog;
import com.synkrato.services.partner.data.jpa.BillingRuleChangeLogRepository;
import com.synkrato.services.partner.util.BillingRuleChangeLogUtil;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
public class BillingRuleChangeLogServiceImpl implements BillingRuleChangeLogService {

  @Autowired private BillingRuleChangeLogRepository changeLogRepository;

  /**
   * Inserts a change log record to db
   *
   * @param changeLog the changelog to be saved
   * @return returns the saved changelog record
   */
  @Override
  public BillingRuleChangeLog createChangeLog(BillingRuleChangeLog changeLog) {
    log.debug("create_change_log {} ", LOGGER_START);

    return changeLogRepository.save(changeLog);
  }

  /**
   * Gets the change log for the given billingRuleId
   *
   * @param billingRuleId billing rule identifier
   * @return returns a list of changes made on the billing rule record.
   */
  @Override
  public List<BillingRuleChangeLogDTO> getChangeLog(String billingRuleId) {
    log.debug("get_change_log {} ", LOGGER_START);

    List<BillingRuleChangeLog> changeLogEntityList = null;

    try {
      changeLogEntityList =
          changeLogRepository.findByBillingRuleId(CommonUtil.getUUIDFromString(billingRuleId));
    } catch (IllegalArgumentException iae) {
      log.error("Invalid BillingRule billing_rule_id={}", billingRuleId, iae);
    }

    if (CollectionUtils.isEmpty(changeLogEntityList)) {
      log.error("billing_rule_id={} not found", billingRuleId);
      throw new DataNotFoundException("billingRule", billingRuleId);
    }

    List<BillingRuleChangeLogDTO> changeLogDTOList =
        BillingRuleChangeLogUtil.buildBillingRuleChangeLogDTO(changeLogEntityList);

    log.debug("get_change_log {} ", LOGGER_END);

    return changeLogDTOList;
  }
}
