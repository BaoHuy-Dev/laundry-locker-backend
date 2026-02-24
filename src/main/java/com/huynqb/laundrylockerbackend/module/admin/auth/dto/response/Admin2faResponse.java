package com.huynqb.laundrylockerbackend.module.admin.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for successful admin 2FA verification. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Admin 2FA verification success response with JWT tokens")
public class Admin2faResponse {

  @Schema(description = "JWT access token for API authentication")
  private String accessToken;

  @Schema(description = "Refresh token for obtaining new access tokens")
  private String refreshToken;

  @Schema(description = "Access token type", example = "Bearer")
  private String tokenType;

  @Schema(description = "Token expiration time in seconds", example = "86400")
  private Long expiresIn;

  @Schema(description = "User roles", example = "[\"ADMIN\"]")
  private Set<String> roles;

  @Schema(description = "Admin user information")
  private AdminUserInfo user;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "Admin user information")
  public static class AdminUserInfo {
    @Schema(description = "User ID", example = "1")
    private Long id;

    @Schema(description = "Admin email", example = "admin@laundrylocker.com")
    private String email;

    @Schema(description = "Admin full name", example = "System Admin")
    private String name;

    @Schema(description = "User roles", example = "[\"ADMIN\"]")
    private Set<String> roles;
  }
}
