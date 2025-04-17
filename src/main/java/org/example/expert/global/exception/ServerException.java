package org.example.expert.global.exception;

import org.springframework.http.HttpStatus;

public class ServerException extends CustomException {
    public ServerException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }
}
