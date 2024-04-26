package com.synkrato.services.partner.data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@SuppressWarnings("squid:S1948")
@Data
@Builder
public class Transformation implements Serializable {

  private static final long serialVersionUID = 1358056450172672138L;

  private String sku;

  private Map<String, Object> transaction;
  private List<Map<String, Object>> groupingRules;
}
