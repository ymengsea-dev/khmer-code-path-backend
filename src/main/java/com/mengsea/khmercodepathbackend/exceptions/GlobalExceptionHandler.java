package com.mengsea.khmercodepathbackend.exceptions;

import com.mengsea.khmercodepathbackend.dto.advices.ApiResponse;
import com.mengsea.khmercodepathbackend.dto.advices.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        ApiResponse<Void> body = ApiResponses.of("SYS-0000", ex.getExceptionCode().getStatusCode(), ex.getMessage(), null);
        return ResponseEntity.status(ex.getExceptionCode().getHttpStatus()).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(field, message);
        });
        ApiResponse<Map<String, String>> body = ApiResponses.of(
                "SYS-1001",
                com.mengsea.khmercodepathbackend.constant.LmsStatusCode.VALIDATION_FAILED,
                "Validation failed",
                errors
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
        ApiResponse<Void> body = ApiResponses.of(
                "SYS-2000",
                com.mengsea.khmercodepathbackend.constant.LmsStatusCode.UNAUTHORIZED,
                "Invalid email or password",
                null
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        ApiResponse<Void> body = ApiResponses.of(
                "SYS-9999",
                com.mengsea.khmercodepathbackend.constant.LmsStatusCode.INTERNAL_SERVER_ERROR,
                ex.getMessage(),
                null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
