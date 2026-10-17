package com.packrat.backend.dto;

import java.util.UUID;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterRequest(@NotNull UUID inviteToken, @NotBlank @Min(3) @Max(32) String username,  @NotBlank @Min(8) @Max(255) String password) {
    
}
