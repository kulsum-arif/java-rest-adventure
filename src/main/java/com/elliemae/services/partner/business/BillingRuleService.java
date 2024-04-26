package com.synkrato.services.partner.business;

import com.synkrato.services.epc.common.dto.BillingRuleDTO;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.MethodArgumentNotValidException;

@Service
public interface BillingRuleService {
  BillingRuleDTO create(BillingRuleDTO billingRuleDTO);

  BillingRuleDTO update(String productId, String id, Map<String, Object> updateBillingRuleMap)
      throws MethodArgumentNotValidException;

  List<BillingRuleDTO> findBillingRules(String productId);
}
