package com.packrat.backend.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import com.packrat.backend.dto.ImageResponse;
import com.packrat.backend.entity.Collection;
import com.packrat.backend.entity.Image;
import com.packrat.backend.entity.Item;
import com.packrat.backend.entity.User;
import com.packrat.backend.exception.ImageNotFoundException;
import com.packrat.backend.repository.ImageRepository;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @Mock
    private ImageRepository imageRepository;
    @Mock
    private ItemService itemService;
    @InjectMocks
    private ImageService imageService;

    private final UUID ownerId = UUID.randomUUID();
    private final UUID otherUserId = UUID.randomUUID();

    private MockMultipartFile image(final int width, final int height, final String format) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB), format, out);
        return new MockMultipartFile("file", "test." + format, format.equals("png") ? "image/png" : "image/jpeg", out.toByteArray());
    }

    private int[] size(final byte[] bytes) throws IOException {
        final BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
        return new int[] { img.getWidth(), img.getHeight() };
    }

    private Item ownedItem() {
        final User owner = new User();
        owner.setId(ownerId);
        final Collection collection = new Collection();
        collection.setUser(owner);
        final Item item = new Item();
        item.setId(UUID.randomUUID());
        item.setCollection(collection);
        return item;
    }

    private Image ownedImage() {
        final Image image = new Image();
        image.setId(UUID.randomUUID());
        image.setItem(ownedItem());
        return image;
    }

    @Test
    void shrinksLongestEdgeTo500AndKeepsAspectRatio() throws IOException {
        assertArrayEquals(new int[] { 500, 250 }, size(imageService.resizeImage(image(2000, 1000, "jpg"))));
        assertArrayEquals(new int[] { 250, 500 }, size(imageService.resizeImage(image(1000, 2000, "png"))));
    }

    @Test
    void doesNotUpscaleSmallImages() throws IOException {
        assertArrayEquals(new int[] { 300, 200 }, size(imageService.resizeImage(image(300, 200, "png"))));
    }

    @Test
    void rejectsNonImages() {
        final MockMultipartFile text = new MockMultipartFile("file", "hello.jpg", "image/jpeg", "hello".getBytes());
        assertEquals("File is not a readable image",
                assertThrows(IllegalArgumentException.class, () -> imageService.resizeImage(text)).getMessage());
    }

    @Test
    void rejectsUnsupportedContentTypes() throws IOException {
        final MockMultipartFile gif = new MockMultipartFile("file", "test.gif", "image/gif", image(10, 10, "png").getBytes());
        assertEquals("Only PNG and JPEG images are supported",
                assertThrows(IllegalArgumentException.class, () -> imageService.resizeImage(gif)).getMessage());
    }

    @Test
    void createImageSavesResizedImageForOwner() throws IOException {
        final Item item = ownedItem();
        when(itemService.fetchItemAndCheckOwnership(ownerId, item.getId())).thenReturn(item);
        when(imageRepository.save(any(Image.class))).thenAnswer(returnsFirstArg());

        final ImageResponse response = imageService.createImage(item.getId(), ownerId, image(2000, 1000, "png"));

        assertEquals(item.getId(), response.itemId());
        assertEquals("image/png", response.contentType());
        verify(imageRepository).save(any(Image.class));
    }

    @Test
    void getImageHidesOtherUsersImages() {
        final Image image = ownedImage();
        when(imageRepository.findById(image.getId())).thenReturn(Optional.of(image));

        assertThrows(ImageNotFoundException.class, () -> imageService.getImage(image.getId(), otherUserId));
    }

    @Test
    void deleteImageDeletesOwnImage() {
        final Image image = ownedImage();
        when(imageRepository.findById(image.getId())).thenReturn(Optional.of(image));

        imageService.deleteImage(image.getId(), ownerId);

        verify(imageRepository).delete(image);
    }

    @Test
    void deleteImageNeverDeletesOtherUsersImages() {
        final Image image = ownedImage();
        when(imageRepository.findById(image.getId())).thenReturn(Optional.of(image));

        assertThrows(ImageNotFoundException.class, () -> imageService.deleteImage(image.getId(), otherUserId));
        verify(imageRepository, never()).delete(any());
    }
}
