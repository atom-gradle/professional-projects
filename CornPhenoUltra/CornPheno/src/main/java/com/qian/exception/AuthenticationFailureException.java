package com.qian.exception;

public class AuthenticationFailureException extends BusinessException {

    public AuthenticationFailureException() {
    }

    public AuthenticationFailureException(String message) {
        super(message);
    }
}
