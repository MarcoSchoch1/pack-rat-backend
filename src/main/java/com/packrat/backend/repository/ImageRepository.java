package com.packrat.backend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.packrat.backend.entity.Image;

public interface ImageRepository extends JpaRepository<Image, UUID>{
    List<Image> findByItemId(UUID id);
}
