package com.synkrato.services.partner.data;

import io.swagger.annotations.ApiModel;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
@ApiModel("Required manifest loan fields")
public class Condition implements Serializable {

  private static final long serialVersionUID = -7086295798654902217L;

  private String type;
  private List<String> required;
}
