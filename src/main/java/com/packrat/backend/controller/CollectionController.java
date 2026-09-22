package com.packrat.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.packrat.backend.dto.CollectionRequest;
import com.packrat.backend.dto.CollectionResponse;
import com.packrat.backend.service.CollectionService;

@RestController
@RequestMapping(name = "api/collection")
public class CollectionController {

    private CollectionService collectionService;

    public CollectionController(final CollectionService collectionService) {
        this.collectionService = collectionService;
    }
    
    @GetMapping 
    public ResponseEntity<List<CollectionResponse>> getCollections(@RequestBody UUID userid) {
        return ResponseEntity.status(HttpStatus.OK).body(collectionService.findCollectionByUserId(userid));
    }

    @PostMapping 
    public ResponseEntity<CollectionResponse> createCollection(@RequestBody CollectionRequest collectionRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(collectionService.addCollection(collectionRequest));
    }
}
