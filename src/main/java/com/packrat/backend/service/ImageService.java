package com.packrat.backend.service;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.packrat.backend.dto.ImageResponse;
import com.packrat.backend.entity.Image;
import com.packrat.backend.entity.Item;
import com.packrat.backend.exception.ImageNotFoundException;
import com.packrat.backend.repository.ImageRepository;

@Service 
public class ImageService {

    private static final int MAX_IMAGE_EDGE_PX = 500;

    private ImageRepository imageRepository;
    
    public ImageService(final ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    public void deleteImage(final UUID imageId, final UUID userId) {
        final Image image = fetchImageAndCheckOwnership(userId, imageId);
        imageRepository.delete(image);
    } 

    // Scales the image down so its longest edge is at most MAX_IMAGE_EDGE_PX (ADR-015). Never scales up.
    protected byte[] resizeImage(final MultipartFile file) {
        final String format = "image/png".equals(file.getContentType()) ? "png" : "jpg";
        try {
            final BufferedImage original = ImageIO.read(file.getInputStream());
            if (original == null) {
                throw new IllegalArgumentException("File is not a readable image");
            }
            final int width = original.getWidth();
            final int height = original.getHeight();
            final double scale = Math.min(1.0, (double) MAX_IMAGE_EDGE_PX / Math.max(width, height));
            final int newWidth = Math.max(1, (int) Math.round(width * scale));
            final int newHeight = Math.max(1, (int) Math.round(height * scale));

            // JPEG can't store transparency, so only PNG gets an alpha channel
            final int type = format.equals("png") ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
            final BufferedImage resized = new BufferedImage(newWidth, newHeight, type);
            final Graphics2D graphics2d = resized.createGraphics();
            graphics2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics2d.drawImage(original, 0, 0, newWidth, newHeight, null);
            graphics2d.dispose();

            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (!ImageIO.write(resized, format, out)) {
                throw new IllegalArgumentException("Unsupported image format: " + format);
            }
            return out.toByteArray();
        } catch (final IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    protected Image fillImage(final Image image, final Item item, final MultipartFile file, final byte[] resizedImageBytes) {
        image.setItem(item);
        image.setOriginalFilename(file.getOriginalFilename());
        image.setFileSizeBytes(resizedImageBytes.length);
        image.setData(resizedImageBytes);
        image.setContentType(file.getContentType());
        return imageRepository.save(image);
    }

    private Image fetchImageAndCheckOwnership(final UUID userId, final UUID imageId) {
        final Image image = imageRepository.findById(imageId).orElseThrow(() -> new ImageNotFoundException(imageId));
        if (!image.getItem().getCollection().getUser().getId().equals(userId)) {
            throw new ImageNotFoundException(imageId);
        }
        return image;
    }

    public ImageResponse toResponse(final Image image, final byte[] resizedImageBytes) {
        return new ImageResponse(image.getId(), image.getItem().getId(), "/api/images/" + image.getId(), image.getOriginalFilename(), image.getContentType(), image.getFileSizeBytes(), image.getCreatedAt());
    }
}
