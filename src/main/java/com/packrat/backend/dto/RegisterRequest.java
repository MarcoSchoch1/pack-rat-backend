package com.packrat.backend.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(@NotBlank UUID token, @NotBlank String username,  @NotBlank String password) {
    
}
