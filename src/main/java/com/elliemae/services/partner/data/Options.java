package com.synkrato.services.partner.data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@SuppressWarnings("squid:S1948")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Options implements Serializable {

  private static final long serialVersionUID = 5515822686716163902L;

  private List<String> requestTypes;
  private Map<String, Object> schema;
}
