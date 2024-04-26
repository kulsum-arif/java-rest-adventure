package com.synkrato.services.partner.data;

import com.synkrato.services.epc.common.dto.enums.AccessScope;
import com.synkrato.services.epc.common.dto.enums.PropertyType;
import java.io.Serializable;
import lombok.Data;

@Data
public class Credential implements Serializable {

  private static final long serialVersionUID = 7072987659021477709L;

  private String id;
  private PropertyType type;
  private String title;
  private String pattern;
  private Integer minimum;
  private Integer maximum;
  private boolean secret;
  private AccessScope scope;
  private boolean required;
}
