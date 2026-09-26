package com.packrat.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.UUID;

import com.packrat.backend.entity.Condition;

import jakarta.annotation.Nullable;

public record ItemResponse(UUID id, UUID collectionId, String name, BigDecimal pricePaid, @Nullable BigDecimal priceNow, Currency currency,
        LocalDate dateAcquired, Condition condition, String marketPlaceLink, LocalDateTime createdAt, LocalDateTime updatedAt) {

}
