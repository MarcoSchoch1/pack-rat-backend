package com.packrat.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.packrat.backend.entity.Collection;

public interface CollectionRepository extends JpaRepository<Collection, UUID>{
    Optional<Collection> findCollectionByUserUsername(String username);
    Optional<Collection> findCollectionByUserId(UUID id);
    List<Collection> findCollectionsByUserId(UUID id);
}
