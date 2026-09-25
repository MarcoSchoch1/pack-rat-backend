package com.packrat.backend.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.packrat.backend.service.ImageService;

@Controller 
@RequestMapping("/api/images")
public class ImageController {

    private ImageService imageService;

    public ImageController(final ImageService imageService) {
        this.imageService = imageService;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteImage(@AuthenticationPrincipal final String userId, @PathVariable final UUID id) {
        imageService.deleteImage(id, UUID.fromString(userId));
        return ResponseEntity.status(HttpStatus.OK).body("Image has been deleted");
    }
    
    //@GetMapping("/{id}")
    //public MultipartFile getImage(@AuthenticationPrincipal final String userId, @PathVariable final UUID id, @RequestParam("file") MultipartFile file) {
    //    imageService.
    //}
}
