package com.qian.exception;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException() {
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
