package com.synkrato.services.partner.data;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

@Data
@Entity
@Table(name = "manifest")
@EqualsAndHashCode
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class Manifest extends BaseEntity implements Serializable {

  private static final long serialVersionUID = 6898377101953444323L;

  @Id private UUID id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id")
  @MapsId
  private Product product;

  @Type(type = "jsonb")
  @Column(name = "origin", columnDefinition = "jsonb")
  private ManifestRequest origin;

  @Type(type = "jsonb")
  @Column(name = "transactions", columnDefinition = "jsonb")
  private List<TransactionEntitlement> transactions;

  @Type(type = "jsonb")
  @Column(name = "findings", columnDefinition = "jsonb")
  private Finding findings;

  @Type(type = "jsonb")
  @Column(name = "serviceEvents", columnDefinition = "jsonb")
  private ServiceEvent serviceEvents;
}
