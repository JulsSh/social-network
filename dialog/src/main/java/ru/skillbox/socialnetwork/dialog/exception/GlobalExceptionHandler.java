package ru.skillbox.socialnetwork.dialog.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(DialogNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleDialogNotFound(DialogNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "error", "DIALOG_NOT_FOUND",
                "error_description", e.getMessage(),
                "timestamp", System.currentTimeMillis()));

    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.badRequest().body(Map.of(
                "error", "INVALID_PARAMETER",
                "error_description", "Invalid value for parameter: " + e.getName(),
                "timestamp", System.currentTimeMillis()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "INTERNAL_ERROR",
                "error_description", "Unexpected error",
                "timestamp", System.currentTimeMillis()));
    }
}
