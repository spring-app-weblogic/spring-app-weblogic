package com.app.exception;

public class ValidationException extends Exception {

    private final String errorCode;

    public ValidationException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

}
