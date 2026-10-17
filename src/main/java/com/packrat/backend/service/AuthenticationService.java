package com.packrat.backend.service;

import java.time.LocalDateTime;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.packrat.backend.dto.LoginRequest;
import com.packrat.backend.dto.LoginResponse;
import com.packrat.backend.dto.RegisterRequest;
import com.packrat.backend.entity.Invite;
import com.packrat.backend.entity.User;
import com.packrat.backend.exception.InviteNotFoundException;
import com.packrat.backend.exception.UsernameDuplicateException;
import com.packrat.backend.repository.InviteRepository;
import com.packrat.backend.repository.UserRepository;

@Service 
public class AuthenticationService {
    
    private final UserRepository userRepository;
    private final InviteRepository inviteRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final InviteService inviteService;

    public AuthenticationService(final UserRepository userRepository, final PasswordEncoder passwordEncoder, final JwtService jwtService, final InviteRepository inviteRepository, final InviteService inviteService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.inviteRepository = inviteRepository;
        this.inviteService = inviteService;
    }

    public LoginResponse login(final LoginRequest loginRequest) {
        final User user = userRepository.findByUsername(loginRequest.username()).orElseThrow(() -> new BadCredentialsException("Bad Credentials"));
        if (passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
            return toResponse(jwtService.buildJwt(user));
        } else {
            throw new BadCredentialsException("Bad Credentials");
        }
    }

    @Transactional 
    public LoginResponse register(final RegisterRequest registerRequest) {
        inviteService.checkInvite(registerRequest.inviteToken());
        final Invite invite = inviteRepository.findById(registerRequest.inviteToken()).orElseThrow(() -> new InviteNotFoundException(registerRequest.inviteToken()));
        if (userRepository.existsByUsername(registerRequest.username())) {
            throw new UsernameDuplicateException(registerRequest.username());
        }
        final User user = new User();
        user.setPassword(passwordEncoder.encode(registerRequest.password()));
        user.setUsername(registerRequest.username());
        userRepository.save(user);
        invite.setUsedAt(LocalDateTime.now());
        invite.setUsedBy(user);
        inviteRepository.save(invite);
        return toResponse(jwtService.buildJwt(user));
    }

    public LoginResponse toResponse(final String jwt) {
        return new LoginResponse(jwt);
    }
}
