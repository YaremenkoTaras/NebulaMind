package com.nebulamind.tradingcore.exception;

/**
 * Exception thrown when risk limits are exceeded
 */
public class RiskLimitExceededException extends RuntimeException {
    public RiskLimitExceededException(String message) {
        super(message);
    }

    public RiskLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}

