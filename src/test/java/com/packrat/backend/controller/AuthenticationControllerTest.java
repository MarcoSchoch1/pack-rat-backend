package com.packrat.backend.controller;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.packrat.backend.dto.LoginRequest;
import com.packrat.backend.dto.RegisterRequest;
import com.packrat.backend.dto.Session;
import com.packrat.backend.security.JwtAuthenticationEntryPoint;
import com.packrat.backend.security.SecurityConfig;
import com.packrat.backend.service.AuthenticationService;
import com.packrat.backend.service.JwtService;

@WebMvcTest(value = AuthenticationController.class, properties = "app.cors.allowed-origins=http://localhost:4200")
@Import({ SecurityConfig.class, JwtAuthenticationEntryPoint.class })
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private AuthenticationService authenticationService;
    @MockitoBean
    private JwtService jwtService;

    // no Authorization header on purpose: login has to work without a token
    private MockHttpServletRequestBuilder login(final String body) {
        return post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body);
    }

    private final Instant expiresAt = Instant.now().plus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.SECONDS);

    // ADR-019: exactly these attributes, Max-Age close to the token's 1h lifetime
    private org.hamcrest.Matcher<String> authCookie(final String token) {
        return allOf(startsWith("jwt=" + token + ";"), containsString("Path=/api"), matchesPattern(".*Max-Age=(3599|3600);.*"),
                containsString("Secure"), containsString("HttpOnly"), containsString("SameSite=Lax"));
    }

    @Test
    void loginIsPublicAndSetsTheTokenAsCookie() throws Exception {
        when(authenticationService.login(new LoginRequest("dev", "secret"))).thenReturn(new Session("a.b.c", expiresAt));

        mockMvc.perform(login("{\"username\":\"dev\",\"password\":\"secret\"}"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, authCookie("a.b.c")))
                .andExpect(jsonPath("$.expiresAt").value(expiresAt.toString()))
                .andExpect(jsonPath("$.jwtAccessToken").doesNotExist())
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("a.b.c"))));
    }

    @Test
    void registerIs201AndSetsTheTokenAsCookie() throws Exception {
        final UUID invite = UUID.randomUUID();
        when(authenticationService.register(new RegisterRequest(invite, "new", "longenough"))).thenReturn(new Session("d.e.f", expiresAt));

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"inviteToken\":\"" + invite + "\",\"username\":\"new\",\"password\":\"longenough\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, authCookie("d.e.f")))
                .andExpect(jsonPath("$.expiresAt").value(expiresAt.toString()))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("d.e.f"))));
    }

    @Test
    void logoutIs204AndExpiresTheCookieOnTheSamePath() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isNoContent())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, allOf(startsWith("jwt=;"), containsString("Path=/api"),
                        containsString("Max-Age=0"), containsString("Secure"), containsString("HttpOnly"), containsString("SameSite=Lax"))));
    }

    @Test
    void badCredentialsAre401() throws Exception {
        when(authenticationService.login(new LoginRequest("dev", "wrong"))).thenThrow(new BadCredentialsException("Bad Credentials"));

        mockMvc.perform(login("{\"username\":\"dev\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Bad Credentials"));
    }

    @Test
    void missingPasswordIs400() throws Exception {
        mockMvc.perform(login("{\"username\":\"dev\"}"))
                .andExpect(status().isBadRequest());
        verify(authenticationService, never()).login(any());
    }

    @Test
    void blankUsernameIs400() throws Exception {
        mockMvc.perform(login("{\"username\":\"  \",\"password\":\"secret\"}"))
                .andExpect(status().isBadRequest());
        verify(authenticationService, never()).login(any());
    }

    @Test
    void corsPreflightFromAllowedOriginPasses() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                .header("Origin", "http://localhost:4200")
                .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    void corsPreflightFromUnknownOriginIsRejected() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                .header("Origin", "http://evil.example")
                .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Credentials"));
    }
}
