package tn.esprit.pi.security;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleBadCredentials_Returns401() {
        ResponseEntity<Map<String, Object>> response = handler.handleBadCredentialsException(new BadCredentialsException("Error"));
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid email or password", response.getBody().get("message"));
    }

    @Test
    void handleAccessDenied_Returns403() {
        ResponseEntity<Map<String, Object>> response = handler.handleAccessDeniedException(new AccessDeniedException("Forbidden"));
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void handleIllegalArgument_Returns400() {
        ResponseEntity<Map<String, Object>> response = handler.handleIllegalArgumentException(new IllegalArgumentException("Wrong arg"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Wrong arg", response.getBody().get("message"));
    }

    @Test
    void handleGenericException_Returns500() {
        ResponseEntity<Map<String, Object>> response = handler.handleGenericException(new Exception("Fatal error"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}