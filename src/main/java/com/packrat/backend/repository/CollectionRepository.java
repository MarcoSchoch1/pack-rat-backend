package com.packrat.backend.repository;

import java.util.Optional;
import java.util.UUID;

public interface CollectionRepository {
    Optional<com.packrat.backend.entity.Collection> findCollectionByUsername(String username);
    Optional<com.packrat.backend.entity.Collection> findCollectionByUserId(UUID userId);
}
