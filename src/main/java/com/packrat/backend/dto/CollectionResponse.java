package com.packrat.backend.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.packrat.backend.entity.User;

public record CollectionResponse(UUID id, User user, String name, BigDecimal totalPricePaid, BigDecimal totalPriceNow) {
    
}
