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
public class Feature implements Serializable {

  private static final long serialVersionUID = -3178751183178167545L;

  private boolean receiveAutomatedTransactionUpdates;
  private Boolean sendFindings;
  private Boolean receiveAutomatedFindingUpdates;
  private Boolean sendServiceEvents;
  private Boolean receiveServiceEvents;
  private Boolean sendResourceTypes;
  private Boolean receiveResourceTypes;
  private Ux ux;
}
