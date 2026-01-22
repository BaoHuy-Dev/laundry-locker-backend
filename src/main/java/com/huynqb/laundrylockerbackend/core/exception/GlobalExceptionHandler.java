package com.huynqb.laundrylockerbackend.core.exception;

import com.huynqb.laundrylockerbackend.core.constant.MessageConstants;
import com.huynqb.laundrylockerbackend.core.dto.ApiResponse;
import com.huynqb.laundrylockerbackend.core.dto.Error;
import com.huynqb.laundrylockerbackend.core.i18n.MessageService;
import com.huynqb.laundrylockerbackend.module.auth.exception.FirebaseAuthException;
import com.huynqb.laundrylockerbackend.module.order.exception.OrderException;
import com.huynqb.laundrylockerbackend.module.payment.exception.PaymentException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Global exception handler for the application.
 * Handles all exceptions and returns standardized error responses.
 *
 * Production-ready features:
 * - Unique trace ID for each error
 * - No stack trace exposure in production
 * - Detailed validation error handling
 * - i18n support for error messages
 * - Comprehensive logging
 */
@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageService messageService;

    @Value("${spring.profiles.active:prod}")
    private String activeProfile;

    private String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    private boolean isProductionProfile() {
        return "prod".equalsIgnoreCase(activeProfile) || "production".equalsIgnoreCase(activeProfile);
    }

    private ApiResponse<Void> buildErrorResponse(String code, String message) {
        return ApiResponse.<Void>builder()
                .success(false)
                .code(code)
                .message(message)
                .build();
    }

    private ApiResponse<Void> buildErrorResponse(String code, String message, List<Error> errors) {
        return ApiResponse.<Void>builder()
                .success(false)
                .code(code)
                .message(message)
                .errors(errors)
                .build();
    }

    // ================================
    // Custom Base Exception Handlers
    // ================================

    /**
     * Handle all BaseException and its subclasses.
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Void>> handleBaseException(BaseException ex, HttpServletRequest request) {
        String traceId = generateTraceId();
        log.warn("Business exception [traceId={}]: {} - {}", traceId, ex.getCode(), ex.getMessage());

        String message = messageService.get(ex.getCode(), ex.getArgs());
        return ResponseEntity.status(ex.getHttpStatus())
                .body(buildErrorResponse(ex.getCode(), message));
    }

    /**
     * Handle ValidationException with field-level errors.
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(ValidationException ex, HttpServletRequest request) {
        String traceId = generateTraceId();
        log.warn("Validation exception [traceId={}]: {}", traceId, ex.getCode());

        List<Error> errors = ex.getFieldErrors().stream()
                .map(fieldError -> Error.builder()
                        .code(fieldError.getCode())
                        .message(fieldError.getMessage())
                        .fields(List.of(fieldError.getField()))
                        .build())
                .collect(Collectors.toList());

        String message = messageService.get(ex.getCode());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildErrorResponse(ex.getCode(), message, errors));
    }

    // ================================
    // Module-specific Exception Handlers
    // ================================

    @ExceptionHandler(FirebaseAuthException.class)
    public ResponseEntity<ApiResponse<Void>> handleFirebaseAuth(FirebaseAuthException ex, HttpServletRequest request) {
        String traceId = generateTraceId();
        log.warn("Firebase auth failed [traceId={}]: {}", traceId, ex.getCode());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(buildErrorResponse(ex.getCode(), messageService.get(ex.getCode())));
    }

    @ExceptionHandler(OrderException.class)
    public ResponseEntity<ApiResponse<Void>> handleOrderException(OrderException ex, HttpServletRequest request) {
        String traceId = generateTraceId();
        log.warn("Order exception [traceId={}]: {}", traceId, ex.getCode());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildErrorResponse(ex.getCode(), messageService.get(ex.getCode())));
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ApiResponse<Void>> handlePaymentException(PaymentException ex, HttpServletRequest request) {
        String traceId = generateTraceId();
        log.warn("Payment exception [traceId={}]: {}", traceId, ex.getCode());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildErrorResponse(ex.getCode(), messageService.get(ex.getCode())));
    }

    // ================================
    // Validation Exception Handlers
    // ================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        String traceId = generateTraceId();
        log.warn("Validation error [traceId={}]: {}", traceId, ex.getMessage());

        List<Error> errors = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.add(Error.builder()
                    .code("FIELD_INVALID")
                    .message(fieldError.getDefaultMessage())
                    .fields(List.of(fieldError.getField()))
                    .build());
        }

        return ResponseEntity.badRequest()
                .body(buildErrorResponse(
                        MessageConstants.E_COM002,
                        messageService.get(MessageConstants.E_COM002),
                        errors));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBindException(BindException ex, HttpServletRequest request) {
        String traceId = generateTraceId();
        log.warn("Bind error [traceId={}]: {}", traceId, ex.getMessage());

        List<Error> errors = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.add(Error.builder()
                    .code("FIELD_INVALID")
                    .message(fieldError.getDefaultMessage())
                    .fields(List.of(fieldError.getField()))
                    .build());
        }

        return ResponseEntity.badRequest()
                .body(buildErrorResponse(
                        MessageConstants.E_COM002,
                        messageService.get(MessageConstants.E_COM002),
                        errors));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParam(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        String traceId = generateTraceId();
        log.warn("Missing parameter [traceId={}]: {}", traceId, ex.getParameterName());

        List<Error> errors = List.of(Error.builder()
                .code("PARAM_MISSING")
                .message(String.format("Required parameter '%s' is missing", ex.getParameterName()))
                .fields(List.of(ex.getParameterName()))
                .build());

        return ResponseEntity.badRequest()
                .body(buildErrorResponse(
                        MessageConstants.E_COM002,
                        messageService.get(MessageConstants.E_COM002),
                        errors));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String traceId = generateTraceId();
        log.warn("Type mismatch [traceId={}]: {} - expected {}", traceId, ex.getName(), ex.getRequiredType());

        String expectedType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        List<Error> errors = List.of(Error.builder()
                .code("TYPE_MISMATCH")
                .message(String.format("Parameter '%s' should be of type %s", ex.getName(), expectedType))
                .fields(List.of(ex.getName()))
                .build());

        return ResponseEntity.badRequest()
                .body(buildErrorResponse(
                        MessageConstants.E_COM002,
                        messageService.get(MessageConstants.E_COM002),
                        errors));
    }

    // ================================
    // Security Exception Handlers
    // ================================

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        String traceId = generateTraceId();
        log.warn("Access denied [traceId={}]: {}", traceId, ex.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(buildErrorResponse(
                        MessageConstants.E_COM003,
                        messageService.get(MessageConstants.E_COM003)));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        String traceId = generateTraceId();
        log.warn("Authentication failed [traceId={}]: {}", traceId, ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(buildErrorResponse(
                        MessageConstants.E_COM004,
                        messageService.get(MessageConstants.E_COM004)));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        String traceId = generateTraceId();
        log.warn("Bad credentials [traceId={}]", traceId);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(buildErrorResponse(
                        MessageConstants.E_COM004,
                        messageService.get(MessageConstants.E_COM004)));
    }

    // ================================
    // HTTP Exception Handlers
    // ================================

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        String traceId = generateTraceId();
        log.warn("Method not supported [traceId={}]: {} on {}", traceId, ex.getMethod(), request.getRequestURI());

        String message = String.format("HTTP method '%s' is not supported for this endpoint", ex.getMethod());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(buildErrorResponse("E_HTTP_METHOD_NOT_ALLOWED", message));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        String traceId = generateTraceId();
        log.warn("Media type not supported [traceId={}]: {}", traceId, ex.getContentType());

        String message = String.format("Media type '%s' is not supported", ex.getContentType());
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(buildErrorResponse("E_UNSUPPORTED_MEDIA_TYPE", message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        String traceId = generateTraceId();
        log.warn("Message not readable [traceId={}]: {}", traceId, ex.getMessage());

        String message = "Malformed JSON request or invalid request body";
        return ResponseEntity.badRequest()
                .body(buildErrorResponse("E_MALFORMED_REQUEST", message));
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ApiResponse<Void>> handleNotFound(Exception ex, HttpServletRequest request) {
        String traceId = generateTraceId();
        log.warn("Resource not found [traceId={}]: {}", traceId, request.getRequestURI());

        String message = String.format("Resource not found: %s", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildErrorResponse("E_RESOURCE_NOT_FOUND", message));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSize(
            MaxUploadSizeExceededException ex, HttpServletRequest request) {
        String traceId = generateTraceId();
        log.warn("File upload size exceeded [traceId={}]", traceId);

        return ResponseEntity.status(HttpStatus.valueOf(413))
                .body(buildErrorResponse("E_FILE_TOO_LARGE", "Uploaded file exceeds maximum allowed size"));
    }

    // ================================
    // JPA/Database Exception Handlers
    // ================================

    @ExceptionHandler(jakarta.persistence.EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleEntityNotFound(
            jakarta.persistence.EntityNotFoundException ex, HttpServletRequest request) {
        String traceId = generateTraceId();
        log.warn("Entity not found [traceId={}]: {}", traceId, ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildErrorResponse("E_ENTITY_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(
            org.springframework.dao.DataIntegrityViolationException ex, HttpServletRequest request) {
        String traceId = generateTraceId();
        log.error("Data integrity violation [traceId={}]: {}", traceId, ex.getMessage());

        String message = isProductionProfile()
                ? "Data integrity violation occurred"
                : ex.getMostSpecificCause().getMessage();
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildErrorResponse("E_DATA_INTEGRITY", message));
    }

    @ExceptionHandler(org.springframework.dao.DataAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataAccessException(
            org.springframework.dao.DataAccessException ex, HttpServletRequest request) {
        String traceId = generateTraceId();
        log.error("Database error [traceId={}]: {}", traceId, ex.getMessage(), ex);

        String message = isProductionProfile()
                ? "A database error occurred. Please try again later."
                : ex.getMessage();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildErrorResponse("E_DATABASE_ERROR", message));
    }

    // ================================
    // Common Exception Handlers
    // ================================

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        String traceId = generateTraceId();
        log.warn("Illegal argument [traceId={}]: {}", traceId, ex.getMessage());

        return ResponseEntity.badRequest()
                .body(buildErrorResponse(
                        MessageConstants.E_COM005,
                        messageService.get(MessageConstants.E_COM005)));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
        String traceId = generateTraceId();
        log.warn("Illegal state [traceId={}]: {}", traceId, ex.getMessage());

        String message = isProductionProfile()
                ? "Operation not allowed in current state"
                : ex.getMessage();
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildErrorResponse("E_ILLEGAL_STATE", message));
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnsupportedOperation(
            UnsupportedOperationException ex, HttpServletRequest request) {
        String traceId = generateTraceId();
        log.warn("Unsupported operation [traceId={}]: {}", traceId, ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(buildErrorResponse("E_NOT_IMPLEMENTED", "This operation is not supported"));
    }

    // ================================
    // Catch-all Exception Handler
    // ================================

    /**
     * Handle all unhandled exceptions.
     * In production, generic message is returned without exposing internal details.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex, HttpServletRequest request) {
        String traceId = generateTraceId();
        log.error("Unexpected error [traceId={}]: {} - {}", traceId, ex.getClass().getSimpleName(), ex.getMessage(), ex);

        String message = isProductionProfile()
                ? messageService.get(MessageConstants.E_COM001)
                : String.format("%s: %s", ex.getClass().getSimpleName(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildErrorResponse(MessageConstants.E_COM001, message));
    }
}
