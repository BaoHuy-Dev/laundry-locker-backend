package com.huynqb.laundrylockerbackend.module.partner.model;

import com.huynqb.laundrylockerbackend.core.model.BaseModel;
import com.huynqb.laundrylockerbackend.module.partner.enums.PartnerStatus;
import com.huynqb.laundrylockerbackend.module.store.model.Store;
import com.huynqb.laundrylockerbackend.module.user.model.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.*;
import lombok.experimental.SuperBuilder;

/** Partner entity - Business partner managing stores and physical staff. */
@Entity
@Table(name = "partners")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Partner extends BaseModel {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** User account linked to this partner. */
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private User user;

  /** Business display name. */
  @Column(nullable = false)
  private String businessName;

  /** Government-issued business registration number. */
  private String businessRegistrationNumber;

  /** Tax identification number. */
  private String taxId;

  /** Physical business address. */
  private String businessAddress;

  /** Primary contact phone. */
  private String contactPhone;

  /** Primary contact email. */
  private String contactEmail;

  /** Current status: PENDING, APPROVED, REJECTED, SUSPENDED. */
  @Enumerated(EnumType.STRING)
  @Builder.Default
  @Column(nullable = false)
  private PartnerStatus status = PartnerStatus.PENDING;

  /** Timestamp when approved by admin. */
  private LocalDateTime approvedAt;

  /** Admin user ID who approved this partner. */
  private Long approvedBy;

  /** Reason if rejected. */
  private String rejectionReason;

  /** Revenue share % (default: 70% partner, 30% platform). */
  @Builder.Default
  @Column(precision = 5, scale = 2)
  private BigDecimal revenueSharePercent = new BigDecimal("70.00");

  /** Stores managed by this partner. */
  @OneToMany(mappedBy = "partner", fetch = FetchType.LAZY)
  @Builder.Default
  private Set<Store> stores = new HashSet<>();

  /** Staff members (for tracking, not login accounts). */
  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "partner_staff",
      joinColumns = @JoinColumn(name = "partner_id"),
      inverseJoinColumns = @JoinColumn(name = "user_id"))
  @Builder.Default
  private Set<User> staff = new HashSet<>();

  /** Internal notes. */
  @Column(length = 1000)
  private String notes;
}
