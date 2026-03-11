package com.huynqb.laundrylockerbackend.module.payment.model;

import com.huynqb.laundrylockerbackend.core.model.BaseModel;
import com.huynqb.laundrylockerbackend.module.order.model.Order;
import com.huynqb.laundrylockerbackend.module.user.model.User;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entity for storing refund information. */
@Entity
@Table(name = "refunds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Refund extends BaseModel {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "payment_id", nullable = false)
  private Payment payment;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  private Order order;

  @Column(name = "amount", nullable = false, precision = 12, scale = 2)
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  @Builder.Default
  private RefundStatus status = RefundStatus.PENDING;

  @Column(name = "reason", columnDefinition = "TEXT")
  private String reason;

  @Column(name = "transaction_id", length = 100)
  private String transactionId;

  @Column(name = "requested_at", nullable = false)
  private LocalDateTime requestedAt;

  @Column(name = "processed_at")
  private LocalDateTime processedAt;

  @Column(name = "notes", columnDefinition = "TEXT")
  private String notes;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "processed_by")
  private User processedBy;

  @Column(name = "gateway_response", columnDefinition = "TEXT")
  private String gatewayResponse;

  public enum RefundStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    CANCELLED
  }

  @PrePersist
  protected void onCreate() {
    if (requestedAt == null) {
      requestedAt = LocalDateTime.now();
    }
  }
}
