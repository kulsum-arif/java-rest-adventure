package com.synkrato.services.partner.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Partner's Transaction Entitlements - Request and Response entitlements */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionEntitlement implements Serializable {

  private static final long serialVersionUID = 2248663580441092047L;

  private List<String> requestTypes;

  private ManifestRequest request;

  private ManifestRequest response;
}
