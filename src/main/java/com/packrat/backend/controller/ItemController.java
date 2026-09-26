package com.packrat.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.packrat.backend.dto.ImageResponse;
import com.packrat.backend.dto.ItemRequest;
import com.packrat.backend.dto.ItemResponse;
import com.packrat.backend.entity.Image;
import com.packrat.backend.service.ItemService;

@Controller 
@RequestMapping("api/items")
public class ItemController {

    private ItemService itemService;
    
    public ItemController(final ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponse> getItem(@AuthenticationPrincipal final String userId, @PathVariable final UUID id) {
        return ResponseEntity.status(HttpStatus.OK).body(itemService.getItem(UUID.fromString(userId), id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ItemResponse> updateItem(@AuthenticationPrincipal final String userId, @PathVariable final UUID id, @RequestBody ItemRequest itemRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(itemService.updateItem(UUID.fromString(userId), id, itemRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteItem(@AuthenticationPrincipal final String userId, @PathVariable final UUID id) {
        itemService.deleteItem(UUID.fromString(userId), id);
        return ResponseEntity.status(HttpStatus.OK).body("Collection was successfuly deleted");
    }

    @GetMapping("/{id}/images") 
    public ResponseEntity<List<ImageResponse>> getImages(@AuthenticationPrincipal final String userId, @PathVariable final UUID id) {
        return ResponseEntity.status(HttpStatus.OK).body(itemService.getImages(id, UUID.fromString(userId)));
    }

    @PostMapping("/{id}/image")
    public ResponseEntity<ImageResponse> createImage(@AuthenticationPrincipal final String userId, @PathVariable final UUID id, @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(itemService.createImage(id, UUID.fromString(userId), file));
    }
}
