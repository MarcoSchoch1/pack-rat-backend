package com.packrat.backend.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import com.packrat.backend.dto.ItemRequest;
import com.packrat.backend.entity.Item;

class ItemServiceTest {

    private final ImageService imageService = new ImageService(null);

    private MockMultipartFile image(final int width, final int height, final String format) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB), format, out);
        return new MockMultipartFile("file", "test." + format, format.equals("png") ? "image/png" : "image/jpeg", out.toByteArray());
    }

    private int[] size(final byte[] bytes) throws IOException {
        final BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
        return new int[] { img.getWidth(), img.getHeight() };
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
    void fillItemOnlyChangesFieldsThatWereSent() {
        final Item item = new Item();
        item.setName("Charizard");
        item.setPricePaid(new BigDecimal("100"));

        new ItemService(null, null, null).fillItem(item, null, new ItemRequest(null, new BigDecimal("80"), null, null, null, null, null));

        assertEquals("Charizard", item.getName());
        assertEquals(new BigDecimal("80"), item.getPricePaid());
    }
}
