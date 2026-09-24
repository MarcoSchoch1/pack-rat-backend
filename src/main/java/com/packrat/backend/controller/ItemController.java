package com.packrat.backend.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.packrat.backend.dto.ItemRequest;
import com.packrat.backend.dto.ItemResponse;
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

    @PutMapping("/{id}")
    public ResponseEntity<ItemResponse> updateItem(@AuthenticationPrincipal final String userId, @PathVariable final UUID id, @RequestBody ItemRequest itemRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(itemService.updateItem(UUID.fromString(userId), id, itemRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteItem(@AuthenticationPrincipal final String userId, @PathVariable final UUID id) {
        itemService.deleteItem(UUID.fromString(userId), id);
        return ResponseEntity.status(HttpStatus.OK).body("Collection was successfuly deleted");
    }
}
