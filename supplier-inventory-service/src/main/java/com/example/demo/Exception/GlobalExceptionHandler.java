package com.example.demo.Exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ✅ HANDLE VALIDATION ERRORS
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    // ✅ RESOURCE NOT FOUND
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleNotFound(ResourceNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    // ✅ INSUFFICIENT STOCK
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<String> handleStock(InsufficientStockException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // ✅ INVALID ORDER STATE
    @ExceptionHandler(InvalidOrderStateException.class)
    public ResponseEntity<String> handleInvalidState(InvalidOrderStateException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // ✅ PAYMENT FAILURE
    @ExceptionHandler(PaymentFailedException.class)
    public ResponseEntity<String> handlePaymentFailure(PaymentFailedException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.PAYMENT_REQUIRED);
    }

    // ✅ UNAUTHORIZED / AUTHENTICATION ERROR
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<String> handleUnauthorized(UnauthorizedException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    // ✅ GENERIC FALLBACK
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleOther(Exception ex) {
        return new ResponseEntity<>("Internal Server Error",
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
