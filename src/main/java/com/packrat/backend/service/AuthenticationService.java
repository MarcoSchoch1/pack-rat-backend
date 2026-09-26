package com.packrat.backend.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.packrat.backend.dto.LoginRequest;
import com.packrat.backend.dto.LoginResponse;
import com.packrat.backend.entity.User;
import com.packrat.backend.repository.UserRepository;

@Service 
public class AuthenticationService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthenticationService(final UserRepository userRepository, final PasswordEncoder passwordEncoder, final JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(final LoginRequest loginRequest) {
        final User user = userRepository.findByUsername(loginRequest.username()).orElseThrow(() -> new BadCredentialsException("Bad Credentials"));
        if (passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
            return toResponse(jwtService.buildJwt(user));
        } else {
            throw new BadCredentialsException("Bad Credentials");
        }
    }

    public LoginResponse toResponse(final String jwt) {
        return new LoginResponse(jwt);
    }
}
