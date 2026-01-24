package com.huynqb.laundrylockerbackend.module.partner.model;

import com.huynqb.laundrylockerbackend.core.model.BaseModel;
import com.huynqb.laundrylockerbackend.module.order.model.Order;
import com.huynqb.laundrylockerbackend.module.partner.enums.AccessCodeAction;
import com.huynqb.laundrylockerbackend.module.partner.enums.AccessCodeStatus;
import java.time.LocalDateTime;

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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/** One-time access code for physical staff to unlock locker boxes. */
@Entity
@Table(name = "staff_access_codes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class StaffAccessCode extends BaseModel {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** 8-character alphanumeric code. */
  @Column(nullable = false, unique = true, length = 20)
  private String code;

  /** Associated order. */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  private Order order;

  /** Partner who generated this code. */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "partner_id", nullable = false)
  private Partner partner;

  /** Action type: COLLECT or RETURN. */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AccessCodeAction action;

  /** Status: ACTIVE, USED, EXPIRED, CANCELLED. */
  @Enumerated(EnumType.STRING)
  @Builder.Default
  @Column(nullable = false)
  private AccessCodeStatus status = AccessCodeStatus.ACTIVE;

  /** Expiration timestamp. */
  private LocalDateTime expiresAt;

  /** Timestamp when code was used. */
  private LocalDateTime usedAt;

  /** Name of staff who used the code. */
  private String staffName;

  /** Partner notes. */
  @Column(length = 500)
  private String notes;

  /** Check if code is valid (active and not expired). */
  public boolean isValid() {
    if (status != AccessCodeStatus.ACTIVE) {
      return false;
    }
    return expiresAt == null || !LocalDateTime.now().isAfter(expiresAt);
  }

  /** Mark code as used. */
  public void markAsUsed(String staffName) {
    this.status = AccessCodeStatus.USED;
    this.usedAt = LocalDateTime.now();
    this.staffName = staffName;
  }

  /** Cancel the code. */
  public void cancel() {
    this.status = AccessCodeStatus.CANCELLED;
  }
}
