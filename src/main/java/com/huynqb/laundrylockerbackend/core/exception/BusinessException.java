package com.huynqb.laundrylockerbackend.core.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a business rule is violated.
 * Returns HTTP 400 Bad Request by default.
 */
public class BusinessException extends BaseException {

    public BusinessException(String code) {
        super(code, HttpStatus.BAD_REQUEST);
    }

    public BusinessException(String code, String message) {
        super(code, HttpStatus.BAD_REQUEST, message);
    }

    public BusinessException(String code, String message, Object... args) {
        super(code, HttpStatus.BAD_REQUEST, message, args);
    }

    public BusinessException(String code, HttpStatus httpStatus) {
        super(code, httpStatus);
    }

    public BusinessException(String code, HttpStatus httpStatus, String message) {
        super(code, httpStatus, message);
    }

    public BusinessException(String code, HttpStatus httpStatus, String message, Object... args) {
        super(code, httpStatus, message, args);
    }

    public BusinessException(String code, String message, Throwable cause) {
        super(code, HttpStatus.BAD_REQUEST, message, cause);
    }
}

