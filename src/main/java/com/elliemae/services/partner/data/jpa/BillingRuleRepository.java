package com.synkrato.services.partner.data.jpa;

import com.synkrato.services.partner.data.BillingRule;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface BillingRuleRepository
    extends JpaRepository<BillingRule, UUID>, JpaSpecificationExecutor<BillingRule> {

  List<BillingRule> findByProductId(UUID productId);

  Optional<BillingRule> findOptionalByProductIdAndId(UUID productId, UUID id);
}
