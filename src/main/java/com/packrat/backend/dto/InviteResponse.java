package com.packrat.backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * InviteResponse
 */
public record InviteResponse(UUID id, UUID userId, LocalDateTime validUnitl) {

}
