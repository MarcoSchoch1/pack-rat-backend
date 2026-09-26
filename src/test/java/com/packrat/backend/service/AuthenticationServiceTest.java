package com.packrat.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.packrat.backend.dto.LoginRequest;
import com.packrat.backend.entity.User;
import com.packrat.backend.repository.UserRepository;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    // real encoder and JwtService: the test checks real hashes and a token that actually parses
    // (strength 4 = the fastest bcrypt setting, keeps the test quick)
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);
    private final JwtService jwtService = new JwtService(Encoders.BASE64.encode(Jwts.SIG.HS256.key().build().getEncoded()), 3_600_000);
    private AuthenticationService authenticationService;

    private final User dev = new User();

    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationService(userRepository, passwordEncoder, jwtService);
        dev.setId(UUID.randomUUID());
        dev.setUsername("dev");
        dev.setPassword(passwordEncoder.encode("secret"));
    }

    @Test
    void correctPasswordReturnsATokenForThatUser() {
        when(userRepository.findByUsername("dev")).thenReturn(Optional.of(dev));

        final String token = authenticationService.login(new LoginRequest("dev", "secret")).jwtAccessToken();

        assertEquals(dev.getId().toString(), jwtService.parseJwt(token).getSubject());
    }

    @Test
    void wrongPasswordIsRejected() {
        when(userRepository.findByUsername("dev")).thenReturn(Optional.of(dev));

        assertThrows(BadCredentialsException.class, () -> authenticationService.login(new LoginRequest("dev", "wrong")));
    }

    @Test
    void unknownUserIsRejected() {
        when(userRepository.findByUsername("nobody")).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> authenticationService.login(new LoginRequest("nobody", "secret")));
    }

    @Test
    void wrongPasswordAndUnknownUserGiveTheSameMessage() {
        // a different message would tell an attacker which usernames exist
        when(userRepository.findByUsername("dev")).thenReturn(Optional.of(dev));
        when(userRepository.findByUsername("nobody")).thenReturn(Optional.empty());

        final String wrongPassword = assertThrows(BadCredentialsException.class,
                () -> authenticationService.login(new LoginRequest("dev", "wrong"))).getMessage();
        final String unknownUser = assertThrows(BadCredentialsException.class,
                () -> authenticationService.login(new LoginRequest("nobody", "secret"))).getMessage();

        assertEquals(wrongPassword, unknownUser);
    }
}
