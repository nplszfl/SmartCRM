package com.smartcrm.common.exception;

/**
 * Exception for unauthorized access.
 */
public class UnauthorizedException extends BusinessException {

    public UnauthorizedException(String message) {
        super(401, message);
    }

    public UnauthorizedException() {
        super(401, "Unauthorized access");
    }
}