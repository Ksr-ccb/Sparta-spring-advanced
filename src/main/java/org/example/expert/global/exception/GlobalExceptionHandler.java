package org.example.expert.global.exception;

import java.time.LocalDateTime;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Map<String, String>> handleCustomException(CustomException ex) {
        Map<String, String> errorResponse = new HashMap<>(); //<필드, 메시지>
        errorResponse.put("error code", String.valueOf(ex.getStatus().value()));
        errorResponse.put("status", String.valueOf(ex.getStatus()));
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("timestamp", String.valueOf(LocalDateTime.now()));

        return ResponseEntity.status(ex.getStatus()).body(errorResponse);
    }
}

