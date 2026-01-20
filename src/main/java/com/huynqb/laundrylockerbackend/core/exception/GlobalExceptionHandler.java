package com.huynqb.laundrylockerbackend.core.exception;

import com.huynqb.laundrylockerbackend.core.constant.MessageConstants;
import com.huynqb.laundrylockerbackend.core.dto.ApiResponse;
import com.huynqb.laundrylockerbackend.core.i18n.MessageService;
import com.huynqb.laundrylockerbackend.module.auth.exception.FirebaseAuthException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

  private final MessageService messageService;

  private ApiResponse<Void> buildErrorResponse(String code, String message) {

    return ApiResponse.<Void>builder().success(false).code(code).message(message).build();
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
    log.error("Unexpected error", ex);

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            buildErrorResponse(
                MessageConstants.E_COM001, messageService.get(MessageConstants.E_COM001)));
  }

  @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
  public ResponseEntity<ApiResponse<Void>> handleValidation(Exception ex) {
    log.warn("Validation error: {}", ex.getMessage());

    return ResponseEntity.badRequest()
        .body(
            buildErrorResponse(
                MessageConstants.E_COM002, messageService.get(MessageConstants.E_COM002)));
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
    log.warn("Access denied: {}", ex.getMessage());

    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(
            buildErrorResponse(
                MessageConstants.E_COM003, messageService.get(MessageConstants.E_COM003)));
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ApiResponse<Void>> handleAuthentication(AuthenticationException ex) {
    log.warn("Authentication failed: {}", ex.getMessage());

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(
            buildErrorResponse(
                MessageConstants.E_COM004, messageService.get(MessageConstants.E_COM004)));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
    log.warn("Illegal argument: {}", ex.getMessage());

    return ResponseEntity.badRequest()
        .body(
            buildErrorResponse(
                MessageConstants.E_COM005, messageService.get(MessageConstants.E_COM005)));
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
    log.warn("Bad credentials: {}", ex.getMessage());

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(
            buildErrorResponse(
                MessageConstants.E_COM004, messageService.get(MessageConstants.E_COM004)));
  }

  @ExceptionHandler(FirebaseAuthException.class)
  public ResponseEntity<ApiResponse<Void>> handleFirebaseAuth(FirebaseAuthException ex) {
    log.warn("Firebase auth failed: {}", ex.getMessage());

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(buildErrorResponse(ex.getCode(), messageService.get(ex.getCode())));
  }

  @ExceptionHandler(com.huynqb.laundrylockerbackend.module.order.exception.OrderException.class)
  public ResponseEntity<ApiResponse<Void>> handleOrderException(
      com.huynqb.laundrylockerbackend.module.order.exception.OrderException ex) {
    log.warn("Order exception: {}", ex.getCode());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(buildErrorResponse(ex.getCode(), messageService.get(ex.getCode())));
  }

  @ExceptionHandler(jakarta.persistence.EntityNotFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleEntityNotFound(
      jakarta.persistence.EntityNotFoundException ex) {
    log.warn("Entity not found: {}", ex.getMessage());

    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(buildErrorResponse("E_NOT_FOUND", ex.getMessage()));
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ApiResponse<Void>> handleIllegalState(IllegalStateException ex) {
    log.warn("Illegal state: {}", ex.getMessage());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(buildErrorResponse("E_BAD_REQUEST", ex.getMessage()));
  }

  @ExceptionHandler(com.huynqb.laundrylockerbackend.module.payment.exception.PaymentException.class)
  public ResponseEntity<ApiResponse<Void>> handlePaymentException(
      com.huynqb.laundrylockerbackend.module.payment.exception.PaymentException ex) {
    log.warn("Payment exception: {}", ex.getCode());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(buildErrorResponse(ex.getCode(), messageService.get(ex.getCode())));
  }
}
