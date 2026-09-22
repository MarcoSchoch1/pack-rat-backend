package com.packrat.backend.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.packrat.backend.entity.User;

import jakarta.annotation.Nullable;

public record CollectionResponse(UUID id, User user, String name, @Nullable BigDecimal totalPricePaid,@Nullable BigDecimal totalPriceNow) {
    
}
