package com.huynqb.laundrylockerbackend.module.locker.model;

import com.huynqb.laundrylockerbackend.core.model.BaseModel;
import com.huynqb.laundrylockerbackend.module.locker.enums.BoxStatus;
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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/** Box entity representing a compartment inside a locker. */
@Entity
@Table(
    name = "boxes",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"locker_id", "box_number"})})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Box extends BaseModel {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "box_number", nullable = false)
  private Integer boxNumber;

  @Builder.Default
  @Column(nullable = false)
  private Boolean isActive = true;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private BoxStatus status;

  @Column(length = 500)
  private String description;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "locker_id", nullable = false)
  private Locker locker;
}
