package com.packrat.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.packrat.backend.dto.ImageResponse;
import com.packrat.backend.dto.ItemRequest;
import com.packrat.backend.dto.ItemResponse;
import com.packrat.backend.exception.ItemNotFoundException;
import com.packrat.backend.security.JwtAuthenticationEntryPoint;
import com.packrat.backend.security.SecurityConfig;
import com.packrat.backend.service.ImageService;
import com.packrat.backend.service.ItemService;
import com.packrat.backend.service.JwtService;

import io.jsonwebtoken.Jwts;

@WebMvcTest(ItemController.class)
@Import({ SecurityConfig.class, JwtAuthenticationEntryPoint.class })
class ItemControllerTest {

    private static final String TOKEN = "valid-token";

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private ItemService itemService;
    @MockitoBean
    private ImageService imageService;
    @MockitoBean
    private JwtService jwtService;

    private final UUID userId = UUID.randomUUID();
    private final UUID itemId = UUID.randomUUID();

    @BeforeEach
    void logIn() {
        when(jwtService.parseJwt(TOKEN)).thenReturn(Jwts.claims().subject(userId.toString()).build());
    }

    private MockHttpServletRequestBuilder authenticated(final MockHttpServletRequestBuilder request) {
        return request.header("Authorization", "Bearer " + TOKEN);
    }

    private MockHttpServletRequestBuilder json(final MockHttpServletRequestBuilder request, final String body) {
        return authenticated(request).contentType(MediaType.APPLICATION_JSON).content(body);
    }

    private ItemResponse itemResponse(final String name) {
        return new ItemResponse(itemId, UUID.randomUUID(), name, BigDecimal.TEN, null, null, null, null, null, null, null);
    }

    @Test
    void getItemReturnsItemOfTheUserInTheToken() throws Exception {
        when(itemService.getItem(userId, itemId)).thenReturn(itemResponse("Charizard"));

        mockMvc.perform(authenticated(get("/api/items/" + itemId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId.toString()))
                .andExpect(jsonPath("$.name").value("Charizard"));
    }

    @Test
    void otherUsersItemIs404() throws Exception {
        when(itemService.getItem(userId, itemId)).thenThrow(new ItemNotFoundException(itemId));

        mockMvc.perform(authenticated(get("/api/items/" + itemId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Item " + itemId + " not found"));
    }

    @Test
    void patchWithOnlySomeFieldsIsAllowed() throws Exception {
        when(itemService.updateItem(eq(userId), eq(itemId), any())).thenReturn(itemResponse("Charizard"));

        mockMvc.perform(json(patch("/api/items/" + itemId), "{\"name\":\"Charizard\"}"))
                .andExpect(status().isOk());
        verify(itemService).updateItem(userId, itemId, new ItemRequest("Charizard", null, null, null, null, null, null));
    }

    @Test
    void patchWithNegativePriceIs400() throws Exception {
        mockMvc.perform(json(patch("/api/items/" + itemId), "{\"pricePaid\":-5}"))
                .andExpect(status().isBadRequest());
        verify(itemService, never()).updateItem(any(), any(), any());
    }

    @Test
    void patchWithBlankNameIs400() throws Exception {
        mockMvc.perform(json(patch("/api/items/" + itemId), "{\"name\":\"  \"}"))
                .andExpect(status().isBadRequest());
        verify(itemService, never()).updateItem(any(), any(), any());
    }

    @Test
    void deleteItemReturns204() throws Exception {
        mockMvc.perform(authenticated(delete("/api/items/" + itemId)))
                .andExpect(status().isNoContent());
        verify(itemService).deleteItem(userId, itemId);
    }

    @Test
    void getImagesListsTheItemsImages() throws Exception {
        final UUID imageId = UUID.randomUUID();
        when(imageService.getImages(itemId, userId)).thenReturn(List.of(
                new ImageResponse(imageId, itemId, "/api/images/" + imageId, "card.png", "image/png", 123, null)));

        mockMvc.perform(authenticated(get("/api/items/" + itemId + "/images")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].url").value("/api/images/" + imageId));
    }

    @Test
    void createImageReturns201() throws Exception {
        final MockMultipartFile file = new MockMultipartFile("file", "card.png", "image/png", new byte[] { 1, 2, 3 });
        final UUID imageId = UUID.randomUUID();
        when(imageService.createImage(eq(itemId), eq(userId), any()))
                .thenReturn(new ImageResponse(imageId, itemId, "/api/images/" + imageId, "card.png", "image/png", 3, null));

        mockMvc.perform(multipart("/api/items/" + itemId + "/images").file(file).header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(imageId.toString()));
    }

    @Test
    void createImageWithUnsupportedTypeIs400() throws Exception {
        final MockMultipartFile file = new MockMultipartFile("file", "card.gif", "image/gif", new byte[] { 1, 2, 3 });
        when(imageService.createImage(eq(itemId), eq(userId), any()))
                .thenThrow(new IllegalArgumentException("Only PNG and JPEG images are supported"));

        mockMvc.perform(multipart("/api/items/" + itemId + "/images").file(file).header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Only PNG and JPEG images are supported"));
    }
}
