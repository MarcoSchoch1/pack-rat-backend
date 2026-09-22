package com.packrat.backend.dto;

import java.util.UUID;

import com.packrat.backend.entity.User;

public record CollectionRequest(UUID id, User user, String name) {
    
}
