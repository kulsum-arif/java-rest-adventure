package com.synkrato.services.partner.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel("Request is manifest data")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ManifestRequest implements Serializable {

  private static final long serialVersionUID = -4176437591799938167L;

  private List<Field> fields;

  private List<Condition> conditions;

  private List<Result> results;

  private List<String> resources;

  private List<Export> exports;

  private Boolean skipDefaultResourceContainerCreation;
}
