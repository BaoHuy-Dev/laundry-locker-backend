package com.huynqb.laundrylockerbackend.core.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when authentication fails.
 * Returns HTTP 401 Unauthorized.
 */
public class UnauthorizedException extends BaseException {

    public UnauthorizedException(String code) {
        super(code, HttpStatus.UNAUTHORIZED);
    }

    public UnauthorizedException(String code, String message) {
        super(code, HttpStatus.UNAUTHORIZED, message);
    }

    public UnauthorizedException(String code, String message, Throwable cause) {
        super(code, HttpStatus.UNAUTHORIZED, message, cause);
    }
}

