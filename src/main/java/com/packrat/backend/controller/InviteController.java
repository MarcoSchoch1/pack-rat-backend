package com.packrat.backend.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.packrat.backend.dto.InviteResponse;

@Controller 
@RequestMapping("api/invites")
public class InviteController {
    
    public InviteController() {

    }

    public RequestEntity<InviteResponse> createInvite(@AuthenticationPrincipal final UUID id) {
        return ResponseEntity.status(HttpStatus.CREATED).body("tbd");
    }
}
