package com.packrat.backend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.packrat.backend.entity.Item;

public interface ItemRepository extends JpaRepository<Item, UUID>{
    List<Item> findByCollectionId(UUID id);
}

