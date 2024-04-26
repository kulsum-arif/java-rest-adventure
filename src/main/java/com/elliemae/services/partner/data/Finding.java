package com.synkrato.services.partner.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel("Finding is optional and part of manifest")
public class Finding implements Serializable {

  private static final long serialVersionUID = -7013295798660703318L;

  private List<FindingType> types;

  private List<String> statuses;

  private List<String> outboundStatuses;
}
