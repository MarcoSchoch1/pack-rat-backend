package com.packrat.backend.dto;

import java.math.BigDecimal;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

// "required" rules only run on create (OnCreate), the "if sent, must be valid" rules (Default) run on create and PATCH
public record ItemRequest(@NotBlank(groups = ItemRequest.OnCreate.class) @Size(max = 255) @Pattern(regexp = ".*\\S.*", message = "name must not be blank") String name,
    @NotNull(groups = ItemRequest.OnCreate.class) @Positive BigDecimal pricePaid, @Nullable @PositiveOrZero BigDecimal priceNow,
    @NotBlank(groups = ItemRequest.OnCreate.class) String currency, @NotBlank(groups = ItemRequest.OnCreate.class) String dateAquired,
    @NotBlank(groups = ItemRequest.OnCreate.class) String condition, @Size(max = 255) String marketPlaceLink) {

    public interface OnCreate {}
}
