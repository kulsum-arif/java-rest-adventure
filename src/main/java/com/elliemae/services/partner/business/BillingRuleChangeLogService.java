package com.synkrato.services.partner.business;

import com.synkrato.services.epc.common.dto.BillingRuleChangeLogDTO;
import com.synkrato.services.partner.data.BillingRuleChangeLog;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public interface BillingRuleChangeLogService {

  BillingRuleChangeLog createChangeLog(BillingRuleChangeLog changeLog);

  List<BillingRuleChangeLogDTO> getChangeLog(String billingRuleId);
}
