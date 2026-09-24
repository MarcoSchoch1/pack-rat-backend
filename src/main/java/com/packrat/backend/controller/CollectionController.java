package com.packrat.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.packrat.backend.dto.CollectionRequest;
import com.packrat.backend.dto.CollectionResponse;
import com.packrat.backend.dto.ItemRequest;
import com.packrat.backend.dto.ItemResponse;
import com.packrat.backend.service.CollectionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("api/collections")
public class CollectionController {

    private CollectionService collectionService;

    public CollectionController(final CollectionService collectionService) {
        this.collectionService = collectionService;
    }
    
    @GetMapping 
    public ResponseEntity<List<CollectionResponse>> getCollections(@AuthenticationPrincipal final String userId) {
        return ResponseEntity.status(HttpStatus.OK).body(collectionService.getCollectionByUserId(UUID.fromString(userId)));
    }

    @PostMapping
    public ResponseEntity<CollectionResponse> createCollection(@AuthenticationPrincipal final String userId, @RequestBody final CollectionRequest collectionRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(collectionService.addCollection(UUID.fromString(userId), collectionRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CollectionResponse> getCollection(@AuthenticationPrincipal final String userId, @PathVariable final UUID id) {
        return ResponseEntity.status(HttpStatus.OK).body(collectionService.getCollection(id, UUID.fromString(userId)));
    }

    @PutMapping ("/{id}")
    public ResponseEntity<CollectionResponse> updateCollection(@AuthenticationPrincipal final String userId, @PathVariable final UUID id, @RequestBody final CollectionRequest collectionRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(collectionService.updateCollection(id, UUID.fromString(userId), collectionRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCollection(@AuthenticationPrincipal final String userId, @PathVariable final UUID id) {
        collectionService.deleteCollection(id, UUID.fromString(userId));
        return ResponseEntity.status(HttpStatus.OK).body("Collection was successfuly deleted");
    }

    @GetMapping("/{id}/overview")
    public ResponseEntity<CollectionResponse> getCollectionOverview(@AuthenticationPrincipal final String userId, @PathVariable final UUID id) {
        return ResponseEntity.status(HttpStatus.OK).body(collectionService.getCollectionOverview(id, UUID.fromString(userId)));
    }

    @GetMapping("/{id}/items")
    public ResponseEntity<List<ItemResponse>> getItemsInCollection(@AuthenticationPrincipal final String userId, @PathVariable final UUID id) {
        return ResponseEntity.status(HttpStatus.OK).body(collectionService.getItemsOfCollection(id, UUID.fromString(userId)));
    }

    @PostMapping("/{id}/item")
    public ResponseEntity<ItemResponse> createItem(@AuthenticationPrincipal final String userId, @PathVariable final UUID id, @Valid @RequestBody final ItemRequest itemRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(collectionService.addItem(id, UUID.fromString(userId), itemRequest));
    }
}
