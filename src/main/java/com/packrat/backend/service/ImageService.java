package com.packrat.backend.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.packrat.backend.entity.Image;
import com.packrat.backend.exception.ImageNotFoundException;
import com.packrat.backend.repository.ImageRepository;

@Service 
public class ImageService {

    private ImageRepository imageRepository;
    
    public ImageService(final ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    public void deleteImage(final UUID imageId, final UUID userId) {
        final Image image = fetchImageAndCheckOwnership(userId, imageId);
        imageRepository.delete(image);
    } 

    //public MultipartFile getImage(final UUID imageId, final UUID userId, MultipartFile file) {
    //    fetchImageAndCheckOwnership(userId, imageId);
    //}

    private Image fetchImageAndCheckOwnership(final UUID userId, final UUID imageId) {
        final Image image = imageRepository.findById(imageId).orElseThrow(() -> new ImageNotFoundException(imageId));
        if (!image.getItem().getCollection().getUser().getId().equals(userId)) {
            throw new ImageNotFoundException(imageId);
        }
        return image;
    }

}
