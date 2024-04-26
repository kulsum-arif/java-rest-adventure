package com.synkrato.services.partner.data;

import io.swagger.annotations.ApiModel;
import java.io.Serializable;
import lombok.Data;

@Data
@ApiModel("Field is part of manifest data")
public class Field implements Serializable {

  private static final long serialVersionUID = -1468059938535004755L;

  private String fieldId;
  private String jsonPath;
  private String description;
}
