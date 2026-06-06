package com.autosalon.api;

import com.autosalon.api.dto.Dtos.ApiError;
import com.autosalon.domain.exception.DomainValidationException;
import com.autosalon.domain.exception.EntityNotFoundException;
import com.autosalon.domain.exception.IncompatibleComponentException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public final class GlobalExceptionHandler {
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(EntityNotFoundException exception) {
        return new ApiError("ENTITY_NOT_FOUND", exception.getMessage(), Instant.now());
    }

    @ExceptionHandler({DomainValidationException.class, IncompatibleComponentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleDomain(RuntimeException exception) {
        return new ApiError("DOMAIN_VALIDATION_FAILED", exception.getMessage(), Instant.now());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));
        return new ApiError("REQUEST_VALIDATION_FAILED", message, Instant.now());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleUnreadable(HttpMessageNotReadableException exception) {
        return new ApiError("REQUEST_BODY_INVALID", exception.getMostSpecificCause().getMessage(), Instant.now());
    }

    private String formatFieldError(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }
}
