package com.packrat.backend.dto;

import java.math.BigDecimal;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record ItemRequest(@NotBlank String name, @NotNull @Positive BigDecimal pricePaid, @Nullable @PositiveOrZero BigDecimal priceNow, 
    @NotBlank String currency, @NotBlank String dateAquired, @NotBlank String condition, String marketPlaceLink) {

}
