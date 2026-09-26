package com.packrat.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.packrat.backend.entity.User;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;

class JwtServiceTest {

    private static final long ONE_HOUR_MS = 3_600_000;

    private final JwtService jwtService = new JwtService(randomSecret(), ONE_HOUR_MS);

    private static String randomSecret() {
        return Encoders.BASE64.encode(Jwts.SIG.HS256.key().build().getEncoded());
    }

    private User user() {
        final User user = new User();
        user.setId(UUID.randomUUID());
        return user;
    }

    @Test
    void tokenParsesBackToTheSameUserId() {
        final User user = user();

        assertEquals(user.getId().toString(), jwtService.parseJwt(jwtService.buildJwt(user)).getSubject());
    }

    @Test
    void tokenExpiresAfterTheConfiguredTime() {
        final var claims = jwtService.parseJwt(jwtService.buildJwt(user()));

        assertEquals(ONE_HOUR_MS, claims.getExpiration().getTime() - claims.getIssuedAt().getTime());
    }

    @Test
    void expiredTokenIsRejected() {
        final JwtService alreadyExpired = new JwtService(randomSecret(), -1000);

        assertThrows(ExpiredJwtException.class, () -> alreadyExpired.parseJwt(alreadyExpired.buildJwt(user())));
    }

    @Test
    void tokenSignedWithAnotherKeyIsRejected() {
        final String foreignToken = new JwtService(randomSecret(), ONE_HOUR_MS).buildJwt(user());

        assertThrows(JwtException.class, () -> jwtService.parseJwt(foreignToken));
    }

    @Test
    void tamperedTokenIsRejected() {
        final String token = jwtService.buildJwt(user());
        // swap the payload (the user id) for another user's, keep the original signature
        final String[] parts = token.split("\\.");
        final String otherPayload = jwtService.buildJwt(user()).split("\\.")[1];
        final String tampered = parts[0] + "." + otherPayload + "." + parts[2];

        assertThrows(JwtException.class, () -> jwtService.parseJwt(tampered));
    }

    @Test
    void garbageIsRejected() {
        assertThrows(JwtException.class, () -> jwtService.parseJwt("not-a-jwt"));
    }
}
