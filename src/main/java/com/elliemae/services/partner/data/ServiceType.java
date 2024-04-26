package com.synkrato.services.partner.data;

import com.synkrato.services.epc.common.dto.enums.SenderType;
import com.synkrato.services.epc.common.dto.enums.ServiceEventType;
import io.swagger.annotations.ApiModel;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
@ApiModel("StatusType is required part of ServiceEvent")
public class ServiceType implements Serializable {

  private static final long serialVersionUID = -6926242562407438035L;

  private String code;

  private String name;

  private List<SenderType> senders;

  private List<ServiceEventType> type;
}
