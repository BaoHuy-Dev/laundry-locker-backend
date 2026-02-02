package com.huynqb.laundrylockerbackend.module.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for deleting user account. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Delete account request")
public class DeleteAccountRequest {

  @NotBlank(message = "Password is required for account deletion")
  @Schema(description = "Current password for verification")
  private String password;

  @Size(max = 500, message = "Reason cannot exceed 500 characters")
  @Schema(description = "Reason for account deletion (optional)")
  private String reason;

  @Schema(
      description = "Confirmation phrase (must type 'DELETE MY ACCOUNT')",
      example = "DELETE MY ACCOUNT")
  private String confirmationPhrase;

  @Schema(description = "Whether to export data before deletion", example = "false")
  @Builder.Default
  private Boolean exportDataBeforeDeletion = false;
}
