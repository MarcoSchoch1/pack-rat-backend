package com.packrat.backend.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.annotation.Nullable;

public record CollectionResponse(UUID id, UUID userId, String name, @Nullable BigDecimal totalPricePaid,@Nullable BigDecimal totalPriceNow) {
    
}
