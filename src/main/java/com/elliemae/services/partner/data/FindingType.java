package com.synkrato.services.partner.data;

import io.swagger.annotations.ApiModel;
import java.io.Serializable;
import lombok.Data;

@Data
@ApiModel("FindingType is required part of findings")
public class FindingType implements Serializable {

  private static final long serialVersionUID = -1916141561007338024L;

  private String code;

  private String name;
}
