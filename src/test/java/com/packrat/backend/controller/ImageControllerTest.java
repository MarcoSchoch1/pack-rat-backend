package com.packrat.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.packrat.backend.entity.Image;
import com.packrat.backend.exception.ImageNotFoundException;
import com.packrat.backend.security.JwtAuthenticationEntryPoint;
import com.packrat.backend.security.SecurityConfig;
import com.packrat.backend.service.ImageService;
import com.packrat.backend.service.JwtService;

import io.jsonwebtoken.Jwts;

@WebMvcTest(ImageController.class)
@Import({ SecurityConfig.class, JwtAuthenticationEntryPoint.class })
class ImageControllerTest {

    private static final String TOKEN = "valid-token";

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private ImageService imageService;
    @MockitoBean
    private JwtService jwtService;

    private final UUID userId = UUID.randomUUID();
    private final UUID imageId = UUID.randomUUID();

    @BeforeEach
    void logIn() {
        when(jwtService.parseJwt(TOKEN)).thenReturn(Jwts.claims().subject(userId.toString()).build());
    }

    private MockHttpServletRequestBuilder authenticated(final MockHttpServletRequestBuilder request) {
        return request.header("Authorization", "Bearer " + TOKEN);
    }

    @Test
    void imagesAreNotPublic() throws Exception {
        mockMvc.perform(get("/api/images/" + imageId))
                .andExpect(status().isUnauthorized());
        verify(imageService, never()).getImage(any(), any());
    }

    @Test
    void getImageReturnsTheBytesWithTheStoredContentType() throws Exception {
        final byte[] data = { 1, 2, 3 };
        final Image image = new Image();
        image.setData(data);
        image.setContentType("image/png");
        when(imageService.getImage(imageId, userId)).thenReturn(image);

        mockMvc.perform(authenticated(get("/api/images/" + imageId)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"))
                .andExpect(content().bytes(data));
    }

    @Test
    void otherUsersImageIs404() throws Exception {
        when(imageService.getImage(imageId, userId)).thenThrow(new ImageNotFoundException(imageId));

        mockMvc.perform(authenticated(get("/api/images/" + imageId)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteImageReturns204() throws Exception {
        mockMvc.perform(authenticated(delete("/api/images/" + imageId)))
                .andExpect(status().isNoContent());
        verify(imageService).deleteImage(imageId, userId);
    }

    @Test
    void deletingOtherUsersImageIs404() throws Exception {
        doThrow(new ImageNotFoundException(imageId)).when(imageService).deleteImage(imageId, userId);

        mockMvc.perform(authenticated(delete("/api/images/" + imageId)))
                .andExpect(status().isNotFound());
    }
}
