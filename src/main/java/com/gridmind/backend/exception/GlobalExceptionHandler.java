package com.gridmind.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── 400 Bad Request: Bean Validation fallida ─────────────────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        // Recopilar todos los mensajes de error de cada campo
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.toList());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 400);
        body.put("error", "Validation Failed");
        body.put("messages", errors);
        body.put("timestamp", System.currentTimeMillis());
        body.put("path", request.getRequestURI());

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // ── 404 Not Found ─────────────────────────────────────────────────────────
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {

        Map<String, Object> error = new HashMap<>();
        error.put("status", 404);
        error.put("message", ex.getMessage());
        error.put("timestamp", System.currentTimeMillis());
        error.put("path", request.getRequestURI());

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    // ── 403 Forbidden ─────────────────────────────────────────────────────────
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {

        Map<String, Object> error = new HashMap<>();
        error.put("status", 403);
        error.put("message", ex.getMessage());
        error.put("timestamp", System.currentTimeMillis());
        error.put("path", request.getRequestURI());

        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    // ── 500 Internal Server Error (fallback) ──────────────────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneral(Exception ex, HttpServletRequest request) {

        Map<String, Object> error = new HashMap<>();
        error.put("status", 500);
        error.put("message", "Internal server error");
        error.put("timestamp", System.currentTimeMillis());
        error.put("path", request.getRequestURI());

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}