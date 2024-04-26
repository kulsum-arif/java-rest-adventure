package com.synkrato.services.partner.data;

import static com.synkrato.services.partner.PartnerServiceConstants.PARTNER_SERVICE;

import com.synkrato.components.microservice.identity.IdentityContext;
import com.synkrato.services.epc.common.util.CommonUtil;
import com.synkrato.services.partner.data.jpa.BillingRuleChangeLogPK;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.springframework.util.StringUtils;

@SuppressWarnings("squid:S1948")
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(BillingRuleChangeLogPK.class)
@Table(name = "billing_rules_change_log")
public class BillingRuleChangeLog implements Serializable {

  private static final long serialVersionUID = 3602771634685880260L;

  @Id
  @Column(name = "changeset_id", nullable = false)
  private int changesetId;

  @Id
  @Column(name = "billing_rule_id", nullable = false)
  private UUID billingRuleId;

  @Column(name = "operation", nullable = false)
  private String operation;

  @Type(type = "jsonb")
  @Column(name = "content", nullable = false, columnDefinition = "jsonb")
  private Map<String, Object> content;

  @Column(name = "comments", nullable = false)
  private String comments;

  @Column(name = "created_by")
  private String createdBy;

  @Column(name = "created")
  private Date created;

  @Version
  @Column(name = "version_number")
  private int versionNumber;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "billing_rule_id", insertable = false, updatable = false)
  private BillingRule billingRule;

  @PrePersist
  public void onPrePersist() {
    setCreated(CommonUtil.getCurrentISODate());
    if (Objects.nonNull(IdentityContext.get())
        && !StringUtils.isEmpty(IdentityContext.get().getSubject())) {
      setCreatedBy(IdentityContext.get().getSubject());
    } else {
      setCreatedBy(PARTNER_SERVICE);
    }
  }
}
