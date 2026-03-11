package com.huynqb.laundrylockerbackend.module.order.model;

import com.huynqb.laundrylockerbackend.core.model.BaseModel;
import com.huynqb.laundrylockerbackend.module.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entity for storing order ratings/reviews. */
@Entity
@Table(
    name = "order_ratings",
    uniqueConstraints = @UniqueConstraint(columnNames = {"order_id", "user_id"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRating extends BaseModel {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  private Order order;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false)
  private Integer rating; // 1-5 stars

  @Column(columnDefinition = "TEXT")
  private String comment;

  @Column(name = "service_rating")
  private Integer serviceRating; // 1-5

  @Column(name = "speed_rating")
  private Integer speedRating; // 1-5

  @Column(name = "staff_rating")
  private Integer staffRating; // 1-5

  @Column(name = "partner_response", columnDefinition = "TEXT")
  private String partnerResponse;

  @Column(name = "responded_at")
  private LocalDateTime respondedAt;

  @Column(name = "responded_by")
  private Long respondedBy;

  @Column(name = "is_visible")
  @Builder.Default
  private Boolean isVisible = true;
}
