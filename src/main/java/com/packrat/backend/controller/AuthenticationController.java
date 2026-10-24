package com.packrat.backend.controller;

import java.time.Duration;
import java.time.Instant;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.packrat.backend.dto.LoginRequest;
import com.packrat.backend.dto.LoginResponse;
import com.packrat.backend.dto.RegisterRequest;
import com.packrat.backend.dto.Session;
import com.packrat.backend.security.JwtAuthenticationFilter;
import com.packrat.backend.service.AuthenticationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService authService;

    public AuthenticationController(final AuthenticationService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid final LoginRequest loginRequest) {
        return withCookie(ResponseEntity.ok(), authService.login(loginRequest));
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@RequestBody @Valid final RegisterRequest registerRequest) {
        return withCookie(ResponseEntity.status(HttpStatus.CREATED), authService.register(registerRequest));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, cookie("", Duration.ZERO).toString()).build();
    }

    private ResponseEntity<LoginResponse> withCookie(final ResponseEntity.BodyBuilder response, final Session session) {
        final ResponseCookie cookie = cookie(session.jwt(), Duration.between(Instant.now(), session.expiresAt()));
        return response.header(HttpHeaders.SET_COOKIE, cookie.toString()).body(new LoginResponse(session.expiresAt()));
    }

    // Secure is set explicitly: behind Nginx and tailscale serve the backend only ever sees plain HTTP (ADR-019)
    private ResponseCookie cookie(final String value, final Duration maxAge) {
        return ResponseCookie.from(JwtAuthenticationFilter.COOKIE_NAME, value)
                .httpOnly(true).secure(true).sameSite("Lax").path("/api").maxAge(maxAge).build();
    }

}
