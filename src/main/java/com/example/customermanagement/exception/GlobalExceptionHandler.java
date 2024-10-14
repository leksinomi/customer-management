package com.example.customermanagement.exception;

import com.example.customermanagement.exception.response.ErrorResponse;
import com.example.customermanagement.exception.response.ValidationErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String HANDLED_EXCEPTION_ATTRIBUTE = "HANDLED_EXCEPTION";
    private static final String VALIDATION_FAILED_MESSAGE = "Validation failed";
    private static final String MALFORMED_JSON_REQUEST_MESSAGE = "Malformed JSON request";
    private static final String DATE_OF_BIRTH_ERROR_MESSAGE = "Date must be in the format yyyy-MM-dd, be a valid date, and not be in the future";

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCustomerNotFoundException(CustomerNotFoundException ex, HttpServletRequest request) {
        logAndSetHandledException(ex, request, "CustomerNotFoundException: {}", false);
        ErrorResponse errorResponse = createErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        logAndSetHandledException(ex, request, "Validation failed: {}", false);

        Map<String, String> validationErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (existing, replacement) -> existing,
                        HashMap::new
                ));

        ValidationErrorResponse validationErrorResponse = createValidationErrorResponse(HttpStatus.BAD_REQUEST, VALIDATION_FAILED_MESSAGE, validationErrors);
        return new ResponseEntity<>(validationErrorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ValidationErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex, HttpServletRequest request) {
        logAndSetHandledException(ex, request, "Malformed JSON request: {}", false);

        Map<String, String> validationErrors = new HashMap<>();
        Throwable cause = Optional.of(ex.getMostSpecificCause()).orElse(ex);
        if (cause instanceof DateTimeParseException) {
            validationErrors.put("dateOfBirth", DATE_OF_BIRTH_ERROR_MESSAGE);
        } else {
            validationErrors.put("request", MALFORMED_JSON_REQUEST_MESSAGE);
        }

        ValidationErrorResponse validationErrorResponse = createValidationErrorResponse(HttpStatus.BAD_REQUEST, VALIDATION_FAILED_MESSAGE, validationErrors);
        return new ResponseEntity<>(validationErrorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        logAndSetHandledException(ex, request, "IllegalArgumentException: {}", false);
        ErrorResponse errorResponse = createErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidSortParameterException.class)
    public ResponseEntity<ErrorResponse> handleInvalidSortParameterException(InvalidSortParameterException ex, HttpServletRequest request) {
        logAndSetHandledException(ex, request, "InvalidSortParameterException: {}", false);
        ErrorResponse errorResponse = createErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(NoResourceFoundException ex, HttpServletRequest request) {
        logAndSetHandledException(ex, request, "NoResourceFoundException: {}", false);
        ErrorResponse errorResponse = createErrorResponse(HttpStatus.NOT_FOUND, "The requested resource was not found.");
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex, HttpServletRequest request) {
        logAndSetHandledException(ex, request, "An unexpected error occurred: {}", true);
        ErrorResponse errorResponse = createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void logAndSetHandledException(Exception ex, HttpServletRequest request, String logMessage, boolean isErrorLog) {
        request.setAttribute(HANDLED_EXCEPTION_ATTRIBUTE, ex);
        if (isErrorLog) {
            logger.error(logMessage, ex.getMessage(), ex);
        } else {
            logger.warn(logMessage, ex.getMessage(), ex);
        }
    }

    private ErrorResponse createErrorResponse(HttpStatus status, String message) {
        return new ErrorResponse(
                status.value(),
                message
        );
    }

    private ValidationErrorResponse createValidationErrorResponse(HttpStatus status, String message, Map<String, String> errors) {
        return new ValidationErrorResponse(
                status.value(),
                message,
                errors
        );
    }
}
