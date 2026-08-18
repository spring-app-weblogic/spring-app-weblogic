package com.app.exception;

public class TransientMessageProcessingException extends RuntimeException {

    private final String errorCode;

    public TransientMessageProcessingException(String errorCode, String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

}

