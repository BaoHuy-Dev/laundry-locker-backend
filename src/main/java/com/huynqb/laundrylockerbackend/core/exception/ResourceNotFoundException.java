package com.huynqb.laundrylockerbackend.core.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a requested resource is not found.
 * Returns HTTP 404 Not Found.
 */
public class ResourceNotFoundException extends BaseException {

    public ResourceNotFoundException(String code) {
        super(code, HttpStatus.NOT_FOUND);
    }

    public ResourceNotFoundException(String code, String message) {
        super(code, HttpStatus.NOT_FOUND, message);
    }

    public ResourceNotFoundException(String code, String message, Object... args) {
        super(code, HttpStatus.NOT_FOUND, message, args);
    }

    /**
     * Create exception for entity not found by ID.
     */
    public static ResourceNotFoundException forEntity(String entityName, Object id, String code) {
        return new ResourceNotFoundException(code,
            String.format("%s not found with id: %s", entityName, id));
    }
}

