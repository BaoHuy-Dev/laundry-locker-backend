package com.huynqb.laundrylockerbackend.core.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when access to a resource is forbidden.
 * Returns HTTP 403 Forbidden.
 */
public class ForbiddenException extends BaseException {

    public ForbiddenException(String code) {
        super(code, HttpStatus.FORBIDDEN);
    }

    public ForbiddenException(String code, String message) {
        super(code, HttpStatus.FORBIDDEN, message);
    }

    public ForbiddenException(String code, String message, Throwable cause) {
        super(code, HttpStatus.FORBIDDEN, message, cause);
    }
}

