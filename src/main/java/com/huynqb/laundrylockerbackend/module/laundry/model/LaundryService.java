package com.huynqb.laundrylockerbackend.module.laundry.model;

import com.huynqb.laundrylockerbackend.core.model.BaseModel;
import com.huynqb.laundrylockerbackend.module.laundry.enums.ServiceStatus;
import com.huynqb.laundrylockerbackend.module.laundry.enums.ServiceType;
import com.huynqb.laundrylockerbackend.module.store.model.Store;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/** LaundryService entity representing a service offered by a store. */
@Entity
@Table(name = "services")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class LaundryService extends BaseModel {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(length = 1000)
  private String image;

  /** Base price of the service. */
  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal price;

  /** Maximum price (for range pricing like 12.000 - 15.000). If null, only base price applies. */
  @Column(precision = 12, scale = 2)
  private BigDecimal maxPrice;

  /** Unit of measurement: kg, piece, order, month, etc. */
  private String unit;

  @Column(length = 2000)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ServiceStatus status;

  /** Type of service for categorization. */
  @Enumerated(EnumType.STRING)
  private ServiceType serviceType;

  /** Whether this is an add-on service (like overnight charge). */
  @Builder.Default private Boolean isAddon = false;

  /** Whether this is a monthly package. */
  @Builder.Default private Boolean isMonthlyPackage = false;

  /** Estimated processing time in hours. */
  private Integer estimatedHours;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "store_id", nullable = false)
  private Store store;
}
