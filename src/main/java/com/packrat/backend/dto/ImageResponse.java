package com.packrat.backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ImageResponse(UUID id, UUID itemId, String url, String originalFilename, String contentType, int fileSizeBytes,
        LocalDateTime createdAt) {

}
