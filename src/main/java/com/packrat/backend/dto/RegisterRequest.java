package com.packrat.backend.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(@NotNull UUID inviteToken, @NotBlank @Size(min = 3, max = 32) String username,  @NotBlank @Size(min = 8, max = 255) String password) {
    
}
