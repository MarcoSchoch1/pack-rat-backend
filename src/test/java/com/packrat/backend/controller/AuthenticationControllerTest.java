package com.packrat.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.packrat.backend.dto.LoginRequest;
import com.packrat.backend.dto.LoginResponse;
import com.packrat.backend.security.JwtAuthenticationEntryPoint;
import com.packrat.backend.security.SecurityConfig;
import com.packrat.backend.service.AuthenticationService;
import com.packrat.backend.service.JwtService;

@WebMvcTest(AuthenticationController.class)
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

    @Test
    void loginIsPublicAndReturnsTheToken() throws Exception {
        when(authenticationService.login(new LoginRequest("dev", "secret"))).thenReturn(new LoginResponse("a.b.c"));

        mockMvc.perform(login("{\"username\":\"dev\",\"password\":\"secret\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwtAccessToken").value("a.b.c"));
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
}
