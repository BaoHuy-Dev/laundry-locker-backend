package com.huynqb.laundrylockerbackend.core.exception;

import com.huynqb.laundrylockerbackend.core.constant.MessageConstants;
import java.util.function.Supplier;

/**
 * Utility class for throwing common exceptions with standardized error codes.
 * Provides convenient factory methods for creating exceptions.
 */
public final class ExceptionUtils {

    private ExceptionUtils() {
        // Utility class
    }

    // ================================
    // Resource Not Found Exceptions
    // ================================

    public static ResourceNotFoundException userNotFound() {
        return new ResourceNotFoundException(MessageConstants.E_USER001);
    }

    public static ResourceNotFoundException userNotFound(Object id) {
        return ResourceNotFoundException.forEntity("User", id, MessageConstants.E_USER001);
    }

    public static ResourceNotFoundException orderNotFound() {
        return new ResourceNotFoundException(MessageConstants.E_ORDER001);
    }

    public static ResourceNotFoundException orderNotFound(Object id) {
        return ResourceNotFoundException.forEntity("Order", id, MessageConstants.E_ORDER001);
    }

    public static ResourceNotFoundException lockerNotFound() {
        return new ResourceNotFoundException(MessageConstants.E_LOCKER001);
    }

    public static ResourceNotFoundException lockerNotFound(Object id) {
        return ResourceNotFoundException.forEntity("Locker", id, MessageConstants.E_LOCKER001);
    }

    public static ResourceNotFoundException boxNotFound() {
        return new ResourceNotFoundException(MessageConstants.E_BOX001);
    }

    public static ResourceNotFoundException boxNotFound(Object id) {
        return ResourceNotFoundException.forEntity("Box", id, MessageConstants.E_BOX001);
    }

    public static ResourceNotFoundException serviceNotFound() {
        return new ResourceNotFoundException(MessageConstants.E_SERVICE001);
    }

    public static ResourceNotFoundException serviceNotFound(Object id) {
        return ResourceNotFoundException.forEntity("Service", id, MessageConstants.E_SERVICE001);
    }

    // ================================
    // Supplier Methods for Optional.orElseThrow()
    // ================================

    public static Supplier<ResourceNotFoundException> userNotFoundSupplier() {
        return ExceptionUtils::userNotFound;
    }

    public static Supplier<ResourceNotFoundException> orderNotFoundSupplier() {
        return ExceptionUtils::orderNotFound;
    }

    public static Supplier<ResourceNotFoundException> lockerNotFoundSupplier() {
        return ExceptionUtils::lockerNotFound;
    }

    public static Supplier<ResourceNotFoundException> boxNotFoundSupplier() {
        return ExceptionUtils::boxNotFound;
    }

    public static Supplier<ResourceNotFoundException> serviceNotFoundSupplier() {
        return ExceptionUtils::serviceNotFound;
    }

    // ================================
    // Business Exceptions
    // ================================

    public static BusinessException businessError(String code) {
        return new BusinessException(code);
    }

    public static BusinessException businessError(String code, String message) {
        return new BusinessException(code, message);
    }

    // ================================
    // Conflict Exceptions
    // ================================

    public static ConflictException emailAlreadyExists() {
        return new ConflictException(MessageConstants.E_AUTH005);
    }

    public static ConflictException duplicateEntity(String entityName, String field, Object value, String code) {
        return ConflictException.forDuplicate(entityName, field, value, code);
    }

    // ================================
    // Unauthorized Exceptions
    // ================================

    public static UnauthorizedException unauthorized() {
        return new UnauthorizedException(MessageConstants.E_COM004);
    }

    public static UnauthorizedException invalidCredentials() {
        return new UnauthorizedException(MessageConstants.E_AUTH001);
    }

    public static UnauthorizedException tokenExpired() {
        return new UnauthorizedException(MessageConstants.E_AUTH002);
    }

    public static UnauthorizedException invalidToken() {
        return new UnauthorizedException(MessageConstants.E_AUTH003);
    }

    // ================================
    // Forbidden Exceptions
    // ================================

    public static ForbiddenException accessDenied() {
        return new ForbiddenException(MessageConstants.E_COM003);
    }

    // ================================
    // Rate Limit Exceptions
    // ================================

    public static RateLimitExceededException rateLimitExceeded() {
        return new RateLimitExceededException(MessageConstants.E_RATE_LIMIT);
    }

    // ================================
    // External Service Exceptions
    // ================================

    public static ExternalServiceException externalServiceError(String serviceName) {
        return new ExternalServiceException(MessageConstants.E_EXTERNAL_SERVICE, serviceName);
    }

    public static ExternalServiceException externalServiceError(String serviceName, String message) {
        return new ExternalServiceException(MessageConstants.E_EXTERNAL_SERVICE, serviceName, message);
    }

    public static ExternalServiceException externalServiceError(String serviceName, String message, Throwable cause) {
        return new ExternalServiceException(MessageConstants.E_EXTERNAL_SERVICE, serviceName, message, cause);
    }
}

