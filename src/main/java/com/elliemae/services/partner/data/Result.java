package com.synkrato.services.partner.data;

import com.fasterxml.jackson.annotation.JsonInclude;
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
public class Result implements Serializable {

  private static final long serialVersionUID = 3102293136179229719L;

  private String action;

  private List<String> formats;

  private List<String> types;

  private List<String> statuses;
}
