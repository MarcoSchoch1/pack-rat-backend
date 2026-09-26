package com.packrat.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.packrat.backend.dto.ImageResponse;
import com.packrat.backend.dto.ItemRequest;
import com.packrat.backend.dto.ItemResponse;
import com.packrat.backend.service.ImageService;
import com.packrat.backend.service.ItemService;

@RestController  
@RequestMapping("api/items")
public class ItemController {

    private final ItemService itemService;
    private final ImageService imageService;
    
    public ItemController(final ItemService itemService, final ImageService imageService) {
        this.itemService = itemService;
        this.imageService = imageService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponse> getItem(@AuthenticationPrincipal final UUID userId, @PathVariable final UUID id) {
        return ResponseEntity.ok(itemService.getItem(userId, id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ItemResponse> updateItem(@AuthenticationPrincipal final UUID userId, @PathVariable final UUID id, @Validated @RequestBody final ItemRequest itemRequest) {
        return ResponseEntity.ok(itemService.updateItem(userId, id, itemRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@AuthenticationPrincipal final UUID userId, @PathVariable final UUID id) {
        itemService.deleteItem(userId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/images") 
    public ResponseEntity<List<ImageResponse>> getImages(@AuthenticationPrincipal final UUID userId, @PathVariable final UUID id) {
        return ResponseEntity.ok(imageService.getImages(id, userId));
    }

    @PostMapping("/{id}/images")
    public ResponseEntity<ImageResponse> createImage(@AuthenticationPrincipal final UUID userId, @PathVariable final UUID id, @RequestParam("file") final MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(imageService.createImage(id, userId, file));
    }
}
