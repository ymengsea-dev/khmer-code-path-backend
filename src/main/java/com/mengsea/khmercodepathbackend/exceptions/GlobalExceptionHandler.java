package com.mengsea.khmercodepathbackend.exceptions;

import com.mengsea.khmercodepathbackend.dto.advices.ApiResponse;
import com.mengsea.khmercodepathbackend.dto.advices.ApiStatus;
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

    /**
     * Handles all business / domain exceptions (wrong credentials, user not found, etc.)
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        ApiResponse<Void> body = ApiResponse.<Void>builder()
                .status(ApiStatus.builder()
                        .code(String.valueOf(ex.getExceptionCode().getCode()))
                        .message(ex.getExceptionCode().getMessage())
                        .build())
                .data(null)
                .build();
        return ResponseEntity.status(ex.getExceptionCode().getCode()).body(body);
    }

    /**
     * Handles @Valid / @Validated bean-validation failures (blank email, missing fields, etc.)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(field, message);
        });
        ApiResponse<Map<String, String>> body = ApiResponse.<Map<String, String>>builder()
                .status(ApiStatus.builder()
                        .code(String.valueOf(HttpStatus.BAD_REQUEST.value()))
                        .message("Validation failed")
                        .build())
                .data(errors)
                .build();
        return ResponseEntity.badRequest().body(body);
    }

    /**
     * Handles Spring Security bad credentials (wrong password via authenticationManager).
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
        ApiResponse<Void> body = ApiResponse.<Void>builder()
                .status(ApiStatus.builder()
                        .code("401")
                        .message("Invalid email or password.")
                        .build())
                .data(null)
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    /**
     * Catch-all for any unexpected exception so the client always gets a JSON response.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        ApiResponse<Void> body = ApiResponse.<Void>builder()
                .status(ApiStatus.builder()
                        .code("500")
                        .message("An unexpected error occurred: " + ex.getMessage())
                        .build())
                .data(null)
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
