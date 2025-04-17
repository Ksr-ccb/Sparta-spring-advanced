package org.example.expert.global.exception;

import org.springframework.http.HttpStatus;

public class AuthException extends CustomException {

    public AuthException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
