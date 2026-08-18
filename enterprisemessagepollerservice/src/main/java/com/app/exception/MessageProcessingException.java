package com.app.exception;

public class MessageProcessingException extends RuntimeException {

    private final String errorCode;

    public MessageProcessingException(String errorCode, String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

}
