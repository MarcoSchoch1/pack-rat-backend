package com.packrat.backend.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.packrat.backend.entity.Image;
import com.packrat.backend.service.ImageService;

@RestController  
@RequestMapping("api/images")
public class ImageController {

    private final ImageService imageService;

    public ImageController(final ImageService imageService) {
        this.imageService = imageService;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImage(@AuthenticationPrincipal final UUID userId, @PathVariable final UUID id) {
        imageService.deleteImage(id, userId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getImage(@AuthenticationPrincipal final UUID userId, @PathVariable final UUID id) {
        final Image image = imageService.getImage(id, userId);
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.parseMediaType(image.getContentType())).body(image.getData());
    }
}
