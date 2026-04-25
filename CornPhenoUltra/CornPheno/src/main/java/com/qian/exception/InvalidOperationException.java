package com.qian.exception;

public class InvalidOperationException extends BusinessException {

    public InvalidOperationException() {
    }

    public InvalidOperationException(String message) {
        super(message);
    }
}
