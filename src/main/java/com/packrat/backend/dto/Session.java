package com.packrat.backend.dto;

import java.time.Instant;

// the token goes into the cookie, only expiresAt goes into the body (ADR-019)
public record Session(String jwt, Instant expiresAt) {
    
}
