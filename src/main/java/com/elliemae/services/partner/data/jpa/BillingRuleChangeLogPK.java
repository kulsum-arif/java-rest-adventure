package com.synkrato.services.partner.data.jpa;

import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class BillingRuleChangeLogPK implements Serializable {

  private static final long serialVersionUID = 7160893329066405801L;

  protected int changesetId;
  protected UUID billingRuleId;
}
