package com.synkrato.services.partner.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Export implements Serializable {

  private static final long serialVersionUID = -1070266459180793810L;

  private String docType;
  private boolean overrideResources;
}
