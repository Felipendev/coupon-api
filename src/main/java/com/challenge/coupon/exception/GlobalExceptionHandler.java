package com.challenge.coupon.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorApiResponse> handleBusinessException(BusinessException ex) {
        log.info("[BusinessException] {} - {}", ex.getStatus(), ex.getMessage());
        return ResponseEntity
                .status(ex.getStatus())
                .body(ErrorApiResponse.builder()
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorApiResponse> handleNoResourceFound(NoResourceFoundException ex) {
        log.debug("Resource not found: {}", ex.getResourcePath());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorApiResponse.builder()
                        .message("Resource not found")
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorApiResponse> handleGenericException(Exception ex) {
        log.error("Exception: ", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorApiResponse.builder()
                        .message("Please contact the system administrator.")
                        .description("INTERNAL SERVER ERROR")
                        .build());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }
}