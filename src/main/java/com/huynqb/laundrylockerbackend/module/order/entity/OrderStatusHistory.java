package com.huynqb.laundrylockerbackend.module.order.entity;

import com.huynqb.laundrylockerbackend.core.model.BaseModel;
import com.huynqb.laundrylockerbackend.module.order.model.Order;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Entity for storing order status history/timeline. */
@Entity
@Table(name = "order_status_history")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusHistory extends BaseModel {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  private Order order;

  @Column(name = "from_status")
  private String fromStatus;

  @Column(name = "to_status", nullable = false)
  private String toStatus;

  @Column(name = "changed_at", nullable = false)
  private LocalDateTime changedAt;

  @Column(name = "changed_by")
  private Long changedBy;

  @Column(name = "actor_type")
  private String actorType; // CUSTOMER, STAFF, PARTNER, SYSTEM, ADMIN

  @Column(name = "actor_name")
  private String actorName;

  @Column(columnDefinition = "TEXT")
  private String note;

  @Column(columnDefinition = "TEXT")
  private String metadata; // JSON metadata for additional info

  @PrePersist
  protected void onCreate() {
    if (changedAt == null) {
      changedAt = LocalDateTime.now();
    }
  }
}
