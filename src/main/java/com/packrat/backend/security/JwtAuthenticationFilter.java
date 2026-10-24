package com.packrat.backend.security;

import java.util.List;
import java.util.UUID;
import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import com.packrat.backend.service.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String COOKIE_NAME = "jwt";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(final JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain) throws ServletException, IOException {
        try {
            final Cookie cookie = WebUtils.getCookie(request, COOKIE_NAME);
            if (cookie == null || cookie.getValue().isBlank()) {
                filterChain.doFilter(request, response);
                return;
            }
            final Claims claims = jwtService.parseJwt(cookie.getValue());
            final UUID userId = UUID.fromString(claims.getSubject());
            final var authentication = new UsernamePasswordAuthenticationToken(userId, null, List.of());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (JwtException e) {
            // invalid/expired token: leave the context unauthenticated,
            // .anyRequest().authenticated() rejects it downstream
            filterChain.doFilter(request, response);
        }
    }
}
