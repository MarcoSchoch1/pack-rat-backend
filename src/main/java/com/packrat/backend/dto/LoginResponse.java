package com.packrat.backend.dto;

import java.time.Instant;

public record LoginResponse(Instant expiresAt) {
    
}
