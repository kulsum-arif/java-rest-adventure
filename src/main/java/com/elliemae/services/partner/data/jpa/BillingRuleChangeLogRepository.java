package com.synkrato.services.partner.data.jpa;

import com.synkrato.services.partner.data.BillingRuleChangeLog;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BillingRuleChangeLogRepository
    extends JpaRepository<BillingRuleChangeLog, String> {
  BillingRuleChangeLog save(BillingRuleChangeLog changeLogEntity);

  List<BillingRuleChangeLog> findByBillingRuleId(UUID billingRuleId);
}
