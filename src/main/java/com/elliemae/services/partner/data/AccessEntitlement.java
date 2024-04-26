package com.synkrato.services.partner.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Partner access accessEntitlement */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccessEntitlement implements Serializable {

  private static final long serialVersionUID = 5776611863265561028L;

  private String allow;

  private String deny;
}
