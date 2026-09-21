package com.packrat.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.packrat.backend.dto.LoginRequest;
import com.packrat.backend.dto.LoginResponse;
import com.packrat.backend.service.AuthenticationService;

@RestController
@RequestMapping("/api/auth/login")
public class AuthenticationController {

    private AuthenticationService authService;

    public AuthenticationController(final AuthenticationService authService) {
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.login(loginRequest));
    }
    
}
