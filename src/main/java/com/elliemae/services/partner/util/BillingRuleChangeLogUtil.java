package com.synkrato.services.partner.util;

import static com.synkrato.services.epc.common.EpcCommonConstants.LOGGER_END;
import static com.synkrato.services.epc.common.EpcCommonConstants.LOGGER_START;

import com.synkrato.services.epc.common.dto.BillingRuleChangeLogDTO;
import com.synkrato.services.partner.data.BillingRuleChangeLog;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

@Slf4j
public class BillingRuleChangeLogUtil {

  private BillingRuleChangeLogUtil() {}

  /**
   * @param changeLogEntityList source changelog entity list
   * @return converted changelog dto list
   */
  public static List<BillingRuleChangeLogDTO> buildBillingRuleChangeLogDTO(
      List<BillingRuleChangeLog> changeLogEntityList) {
    log.debug("build_billing_rule_change_log_dto {}", LOGGER_START);

    List<BillingRuleChangeLogDTO> changeLogDTOList = null;

    if (Objects.nonNull(changeLogEntityList)) {
      changeLogDTOList = new ArrayList<>();

      for (BillingRuleChangeLog changelog : changeLogEntityList) {
        BillingRuleChangeLogDTO changeLogDTO = BillingRuleChangeLogDTO.builder().build();
        BeanUtils.copyProperties(changelog, changeLogDTO);
        changeLogDTO.setBillingRuleId(changelog.getBillingRuleId().toString());
        changeLogDTO.setCreated(changelog.getCreated());
        changeLogDTOList.add(changeLogDTO);
      }
    }

    log.debug("build_billing_rule_change_log_dto {}", LOGGER_END);

    return changeLogDTOList;
  }
}
