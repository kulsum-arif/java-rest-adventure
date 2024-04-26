package com.synkrato.services.partner.data;

import static com.synkrato.services.partner.PartnerServiceConstants.ACCESS_FILTER_NAME;
import static com.synkrato.services.partner.PartnerServiceConstants.ACCESS_FILTER_PARAM_TENANT_ID;

import com.synkrato.services.epc.common.dto.enums.TagType;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "product")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@FilterDef(
    name = ACCESS_FILTER_NAME,
    parameters = @ParamDef(name = ACCESS_FILTER_PARAM_TENANT_ID, type = "string"))
@Filter(
    name = ACCESS_FILTER_NAME,
    condition =
        ":"
            + ACCESS_FILTER_PARAM_TENANT_ID
            + " ~ LOWER(cast(access_entitlements->>'allow' as text))")
@Filter(
    name = ACCESS_FILTER_NAME,
    condition =
        ":"
            + ACCESS_FILTER_PARAM_TENANT_ID
            + " !~ LOWER(cast(access_entitlements->>'deny' as text))")
@NamedEntityGraph(name = "Product.manifest", attributeNodes = @NamedAttributeNode("manifest"))
public class Product extends BaseEntity implements Serializable {

  private static final long serialVersionUID = -6601500212792187028L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
  private UUID id;

  @Column(name = "partner_id", nullable = false)
  private String partnerId;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "listing_name", nullable = false)
  private String listingName;

  @Column(name = "environment", nullable = false)
  private String environment;

  @Column(name = "status", nullable = false)
  private String status;

  @Column(name = "interface_url")
  private String interfaceUrl;

  @Column(name = "admin_interface_url")
  private String adminInterfaceUrl;

  @Column(name = "integration_type")
  private String integrationType;

  @Column(name = "partner_url")
  private String partnerUrl;

  @Column(name = "extension_limit")
  private Integer extensionLimit;

  @Column(name = "tags", columnDefinition = "jsonb")
  @Type(type = "jsonb")
  private Map<TagType, List<String>> tags;

  @Column(name = "request_types", nullable = false, columnDefinition = "jsonb")
  @Type(type = "jsonb")
  private List<String> requestTypes;

  @Column(name = "webhook_subscriptions", columnDefinition = "jsonb")
  @Type(type = "jsonb")
  private List<String> webhookSubscriptions;

  @Column(name = "access_entitlements", columnDefinition = "jsonb")
  @Type(type = "jsonb")
  private AccessEntitlement accessEntitlements;

  @Column(name = "credentials", columnDefinition = "jsonb")
  @Type(type = "jsonb")
  private List<Credential> credentials;

  @Column(name = "options", columnDefinition = "jsonb")
  @Type(type = "jsonb")
  private List<Options> options;

  @OneToOne(mappedBy = "product", cascade = CascadeType.ALL)
  private Manifest manifest;

  @Column(name = "feature", columnDefinition = "jsonb")
  @Type(type = "jsonb")
  private Feature feature;

  @Column(name = "additional_links", columnDefinition = "jsonb")
  @Type(type = "jsonb")
  private List<AdditionalLink> additionalLinks;

  @PrePersist
  void preInsert() {
    if (Objects.isNull(this.extensionLimit)) {
      this.extensionLimit = 0;
    }
  }
}
