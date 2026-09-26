package com.packrat.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.packrat.backend.dto.CollectionRequest;
import com.packrat.backend.dto.CollectionResponse;
import com.packrat.backend.exception.CollectionNotFoundException;
import com.packrat.backend.security.JwtAuthenticationEntryPoint;
import com.packrat.backend.security.SecurityConfig;
import com.packrat.backend.service.CollectionService;
import com.packrat.backend.service.ItemService;
import com.packrat.backend.service.JwtService;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;

@WebMvcTest(CollectionController.class)
@Import({ SecurityConfig.class, JwtAuthenticationEntryPoint.class })
class CollectionControllerTest {

    private static final String TOKEN = "valid-token";

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private CollectionService collectionService;
    @MockitoBean
    private ItemService itemService;
    @MockitoBean
    private JwtService jwtService;

    private final UUID userId = UUID.randomUUID();
    private final UUID collectionId = UUID.randomUUID();

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

    @Test
    void requestWithoutTokenIs401() throws Exception {
        mockMvc.perform(get("/api/collections"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidOrExpiredTokenIs401() throws Exception {
        when(jwtService.parseJwt("expired-token")).thenThrow(new ExpiredJwtException(null, null, "expired"));

        mockMvc.perform(get("/api/collections").header("Authorization", "Bearer expired-token"))
                .andExpect(status().isUnauthorized());
        verify(collectionService, never()).getCollectionsByUserId(any());
    }

    @Test
    void createCollectionReturns201ForTheUserInTheToken() throws Exception {
        when(collectionService.addCollection(userId, new CollectionRequest("Pokemon")))
                .thenReturn(new CollectionResponse(collectionId, userId, "Pokemon", null, null));

        mockMvc.perform(json(post("/api/collections"), "{\"name\":\"Pokemon\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Pokemon"))
                .andExpect(jsonPath("$.userId").value(userId.toString()));
    }

    @Test
    void createCollectionWithoutNameIs400() throws Exception {
        mockMvc.perform(json(post("/api/collections"), "{}"))
                .andExpect(status().isBadRequest());
        verify(collectionService, never()).addCollection(any(), any());
    }

    @Test
    void createCollectionWithBlankNameIs400() throws Exception {
        mockMvc.perform(json(post("/api/collections"), "{\"name\":\"  \"}"))
                .andExpect(status().isBadRequest());
        verify(collectionService, never()).addCollection(any(), any());
    }

    @Test
    void updateCollectionWithoutNameIs400() throws Exception {
        mockMvc.perform(json(patch("/api/collections/" + collectionId), "{}"))
                .andExpect(status().isBadRequest());
        verify(collectionService, never()).updateCollection(any(), any(), any());
    }

    @Test
    void otherUsersCollectionIs404() throws Exception {
        when(collectionService.getCollection(collectionId, userId)).thenThrow(new CollectionNotFoundException(collectionId));

        mockMvc.perform(authenticated(get("/api/collections/" + collectionId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Collection " + collectionId + " not found"));
    }

    @Test
    void deleteCollectionReturns204() throws Exception {
        mockMvc.perform(authenticated(delete("/api/collections/" + collectionId)))
                .andExpect(status().isNoContent());
        verify(collectionService).deleteCollection(collectionId, userId);
    }

    @Test
    void createItemWithMissingFieldsIs400() throws Exception {
        mockMvc.perform(json(post("/api/collections/" + collectionId + "/items"), "{\"name\":\"Pikachu\"}"))
                .andExpect(status().isBadRequest());
        verify(itemService, never()).addItem(any(), any(), any());
    }
}
