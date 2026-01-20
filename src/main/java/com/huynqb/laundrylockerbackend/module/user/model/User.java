package com.huynqb.laundrylockerbackend.module.user.model;

import com.huynqb.laundrylockerbackend.core.model.BaseModel;
import com.huynqb.laundrylockerbackend.module.user.enums.AuthProvider;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class User extends BaseModel {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true) // Nullable for phone-only users
  private String email;

  private String name; // Deprecated - use firstName + lastName

  private String firstName;

  private String lastName;

  private LocalDate birthday;

  @Column(length = 1000)
  private String imageUrl;

  @Column(length = 500)
  private String password; // Nullable for phone users

  @Enumerated(EnumType.STRING)
  private AuthProvider provider;

  private String providerId;

  @Builder.Default
  @Column(nullable = false)
  private Boolean emailVerified = false;

  @Builder.Default private Boolean phoneVerified = false;

  @Column(unique = true)
  private String phoneNumber;

  @Builder.Default
  @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
  @JoinTable(
      name = "user_roles",
      schema = "laundry_locker_schema",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id"))
  private Set<Role> roles = new HashSet<>();
}
