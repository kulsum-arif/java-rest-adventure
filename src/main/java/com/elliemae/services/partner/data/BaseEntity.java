package com.synkrato.services.partner.data;

import static com.synkrato.services.partner.PartnerServiceConstants.PARTNER_SERVICE;

import com.synkrato.components.microservice.identity.IdentityContext;
import com.synkrato.services.epc.common.util.CommonUtil;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Version;
import lombok.Data;
import org.springframework.util.StringUtils;

@Data
@MappedSuperclass
public class BaseEntity implements Serializable {

  private static final long serialVersionUID = -3227785217128050501L;

  @Column(name = "created_by")
  private String createdBy;

  @Column(name = "created")
  private Date created;

  @Column(name = "updated_by")
  private String updatedBy;

  @Column(name = "updated")
  private Date updated;

  @Version
  @Column(name = "version_number")
  private int versionNumber;

  @PrePersist
  public void onPrePersist() {
    setCreated(CommonUtil.getCurrentISODate());

    if (IdentityContext.get() != null && !StringUtils.isEmpty(IdentityContext.get().getSubject())) {
      setCreatedBy(IdentityContext.get().getSubject());
    } else {
      setCreatedBy(PARTNER_SERVICE);
    }
  }

  @PreUpdate
  public void onPreUpdate() {
    setUpdated(CommonUtil.getCurrentISODate());

    if (IdentityContext.get() != null && !StringUtils.isEmpty(IdentityContext.get().getSubject())) {
      setUpdatedBy(IdentityContext.get().getSubject());
    } else {
      setUpdatedBy(PARTNER_SERVICE);
    }
  }
}
