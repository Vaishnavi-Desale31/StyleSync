package com.project.stylesync_ai.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler();

    @Test
    void handleIllegalArgument_shouldReturnBadRequest() {
        IllegalArgumentException exception =
                new IllegalArgumentException("Invalid quantity");

        ResponseEntity<Map<String, String>> response =
                handler.handleIllegalArgument(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Bad Request", response.getBody().get("error"));
        assertEquals("Invalid quantity", response.getBody().get("message"));
    }

    @Test
    void handleResourceNotFound_shouldReturnNotFound() {
        ResourceNotFoundException exception =
                new ResourceNotFoundException("Product not found");

        ResponseEntity<Map<String, String>> response =
                handler.handleResourceNotFound(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(
                "Product not found",
                response.getBody().get("message")
        );
    }

    @Test
    void handleResponseStatus_shouldPreserveStatusAndReason() {
        ResponseStatusException exception =
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cart not found"
                );

        ResponseEntity<Map<String, String>> response =
                handler.handleResponseStatus(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(
                exception.getStatusCode().toString(),
                response.getBody().get("error")
        );
        assertEquals(
                "Cart not found",
                response.getBody().get("message")
        );
    }

    @Test
    void handleValidationErrors_shouldReturnFieldErrors() {
        MethodArgumentNotValidException exception =
                mock(MethodArgumentNotValidException.class);

        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError = new FieldError(
                "productRequest",
                "price",
                "must be positive"
        );

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors())
                .thenReturn(List.of(fieldError));

        ResponseEntity<Map<String, String>> response =
                handler.handleValidationErrors(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(
                "must be positive",
                response.getBody().get("price")
        );
    }

    @Test
    void handleUnexpectedException_shouldReturnSafeInternalServerError() {
        Exception exception =
                new RuntimeException("Sensitive database details");

        ResponseEntity<Map<String, String>> response =
                handler.handleUnexpectedException(exception);

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                response.getStatusCode()
        );
        assertEquals(
                "Internal Server Error",
                response.getBody().get("error")
        );
        assertEquals(
                "An unexpected error occurred",
                response.getBody().get("message")
        );
    }
}