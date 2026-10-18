package com.example.music.exception;

import com.example.music.dto.ApiError;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/** One place that turns exceptions into a uniform JSON body {status, message}. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class) // @Valid failed
    ResponseEntity<ApiError> validation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream().map(f -> f.getDefaultMessage()).findFirst().orElse("Невалидни данни");
        return body(HttpStatus.BAD_REQUEST, msg);
    }

    @ExceptionHandler(ResponseStatusException.class) // thrown by services: 400/404/409...
    ResponseEntity<ApiError> status(ResponseStatusException e) {
        HttpStatus s = HttpStatus.valueOf(e.getStatusCode().value());
        return body(s, e.getReason() != null ? e.getReason() : s.getReasonPhrase());
    }

    @ExceptionHandler(DataIntegrityViolationException.class) // unique constraint etc.
    ResponseEntity<ApiError> integrity(DataIntegrityViolationException e) {
        return body(HttpStatus.CONFLICT, "Записът нарушава ограничение (например повтарящи се данни)");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class) // malformed JSON, bad enum value
    ResponseEntity<ApiError> unreadable(HttpMessageNotReadableException e) {
        return body(HttpStatus.BAD_REQUEST, "Невалидна заявка");
    }

    private static ResponseEntity<ApiError> body(HttpStatus s, String message) {
        return ResponseEntity.status(s).body(new ApiError(s.value(), message));
    }
}
