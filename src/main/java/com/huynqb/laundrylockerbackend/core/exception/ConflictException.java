package com.huynqb.laundrylockerbackend.core.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when there is a conflict with the current state.
 * Returns HTTP 409 Conflict.
 */
public class ConflictException extends BaseException {

    public ConflictException(String code) {
        super(code, HttpStatus.CONFLICT);
    }

    public ConflictException(String code, String message) {
        super(code, HttpStatus.CONFLICT, message);
    }

    public ConflictException(String code, String message, Object... args) {
        super(code, HttpStatus.CONFLICT, message, args);
    }

    /**
     * Create exception for duplicate entity.
     */
    public static ConflictException forDuplicate(String entityName, String field, Object value, String code) {
        return new ConflictException(code,
            String.format("%s already exists with %s: %s", entityName, field, value));
    }
}

