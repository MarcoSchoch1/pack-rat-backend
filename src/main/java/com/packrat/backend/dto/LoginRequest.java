package com.packrat.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank @Valid String username,  @NotBlank @Valid String password) {

} 
