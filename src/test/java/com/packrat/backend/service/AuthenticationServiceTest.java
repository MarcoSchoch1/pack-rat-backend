package com.packrat.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
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
import com.packrat.backend.dto.RegisterRequest;
import com.packrat.backend.entity.Invite;
import com.packrat.backend.entity.User;
import com.packrat.backend.exception.InviteNotFoundException;
import com.packrat.backend.exception.UsernameDuplicateException;
import com.packrat.backend.repository.InviteRepository;
import com.packrat.backend.repository.UserRepository;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    final static private String FRONTEND_URL = "http://localhost:4200";

    @Mock
    private UserRepository userRepository;
    @Mock 
    private InviteRepository inviteRepository;

    // real encoder and JwtService: the test checks real hashes and a token that actually parses
    // (strength 4 = the fastest bcrypt setting, keeps the test quick)
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);
    private final JwtService jwtService = new JwtService(Encoders.BASE64.encode(Jwts.SIG.HS256.key().build().getEncoded()), 3_600_000);
    private AuthenticationService authenticationService;
    private InviteService inviteService;

    private final User dev = new User();

    @BeforeEach
    void setUp() {
        inviteService = new InviteService(inviteRepository, userRepository, FRONTEND_URL);
        authenticationService = new AuthenticationService(userRepository, passwordEncoder, jwtService, inviteRepository, inviteService);
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

    private Invite invite(final LocalDateTime validUntil) {
        final Invite invite = new Invite();
        invite.setId(UUID.randomUUID());
        invite.setCreatedBy(dev);
        invite.setExpiresAt(validUntil);
        when(inviteRepository.findById(invite.getId())).thenReturn(Optional.of(invite));
        return invite;
    }

    @Test
    void registerWithValidInviteCreatesUserAndMarksInviteUsed() {
        final Invite invite = invite(LocalDateTime.now().plusDays(1));
        when(userRepository.existsByUsername("new")).thenReturn(false);
        // the real repository assigns the id on save; JwtService needs it for the subject
        when(userRepository.save(any(User.class))).thenAnswer(call -> {
            final User user = call.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });

        final String token = authenticationService.register(new RegisterRequest(invite.getId(), "new", "secret")).jwtAccessToken();

        final User created = invite.getUsedBy();
        assertNotNull(created);
        assertNotNull(invite.getUsedAt());
        assertEquals("new", created.getUsername());
        assertTrue(passwordEncoder.matches("secret", created.getPassword()));
        assertEquals(created.getId().toString(), jwtService.parseJwt(token).getSubject());
        verify(inviteRepository).save(invite);
    }

    @Test
    void registerWithUnknownInviteIsRejected() {
        final UUID token = UUID.randomUUID();
        when(inviteRepository.findById(token)).thenReturn(Optional.empty());

        assertThrows(InviteNotFoundException.class, () -> authenticationService.register(new RegisterRequest(token, "new", "secret")));
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerWithExpiredInviteIsRejected() {
        final Invite invite = invite(LocalDateTime.now().minusMinutes(1));

        assertThrows(InviteNotFoundException.class, () -> authenticationService.register(new RegisterRequest(invite.getId(), "new", "secret")));
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerWithUsedInviteIsRejected() {
        final Invite invite = invite(LocalDateTime.now().plusDays(1));
        invite.setUsedBy(dev);
        invite.setUsedAt(LocalDateTime.now());

        assertThrows(InviteNotFoundException.class, () -> authenticationService.register(new RegisterRequest(invite.getId(), "new", "secret")));
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerWithTakenUsernameIsRejectedAndInviteStaysUnused() {
        final Invite invite = invite(LocalDateTime.now().plusDays(1));
        when(userRepository.existsByUsername("dev")).thenReturn(true);

        assertThrows(UsernameDuplicateException.class, () -> authenticationService.register(new RegisterRequest(invite.getId(), "dev", "secret")));
        verify(userRepository, never()).save(any());
        assertNull(invite.getUsedBy());
    }
}
