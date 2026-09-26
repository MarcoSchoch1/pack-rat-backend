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
import org.springframework.web.bind.annotation.RestController;

import com.packrat.backend.dto.CollectionRequest;
import com.packrat.backend.dto.CollectionResponse;
import com.packrat.backend.dto.ItemRequest;
import com.packrat.backend.dto.ItemResponse;
import com.packrat.backend.service.CollectionService;
import com.packrat.backend.service.ItemService;

import jakarta.validation.Valid;
import jakarta.validation.groups.Default;

@RestController
@RequestMapping("api/collections")
public class CollectionController {

    private final CollectionService collectionService;
    private final ItemService itemService;

    public CollectionController(final CollectionService collectionService, final ItemService itemService) {
        this.collectionService = collectionService;
        this.itemService = itemService;
    }
    
    @GetMapping 
    public ResponseEntity<List<CollectionResponse>> getCollections(@AuthenticationPrincipal final UUID userId) {
        return ResponseEntity.ok(collectionService.getCollectionsByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<CollectionResponse> createCollection(@AuthenticationPrincipal final UUID userId, @Valid @RequestBody final CollectionRequest collectionRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(collectionService.addCollection(userId, collectionRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CollectionResponse> getCollection(@AuthenticationPrincipal final UUID userId, @PathVariable final UUID id) {
        return ResponseEntity.ok(collectionService.getCollection(id, userId));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CollectionResponse> updateCollection(@AuthenticationPrincipal final UUID userId, @PathVariable final UUID id, @Valid @RequestBody final CollectionRequest collectionRequest) {
        return ResponseEntity.ok(collectionService.updateCollection(id, userId, collectionRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCollection(@AuthenticationPrincipal final UUID userId, @PathVariable final UUID id) {
        collectionService.deleteCollection(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/overview")
    public ResponseEntity<CollectionResponse> getCollectionOverview(@AuthenticationPrincipal final UUID userId, @PathVariable final UUID id) {
        return ResponseEntity.ok(collectionService.getCollectionOverview(id, userId));
    }

    @GetMapping("/{id}/items")
    public ResponseEntity<List<ItemResponse>> getItemsInCollection(@AuthenticationPrincipal final UUID userId, @PathVariable final UUID id) {
        return ResponseEntity.ok(itemService.getItemsOfCollection(id, userId));
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<ItemResponse> createItem(@AuthenticationPrincipal final UUID userId, @PathVariable final UUID id, @Validated({ ItemRequest.OnCreate.class, Default.class }) @RequestBody final ItemRequest itemRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(itemService.addItem(id, userId, itemRequest));
    }
}
