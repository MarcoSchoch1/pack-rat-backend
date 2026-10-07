package com.packrat.backend.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.packrat.backend.dto.InviteResponse;
import com.packrat.backend.service.InviteService;

@Controller 
@RequestMapping("api/invites")
public class InviteController {

    private InviteService inviteService;
    
    public InviteController(final InviteService inviteService) {
        this.inviteService = inviteService;
    }

    @PostMapping
    public ResponseEntity<InviteResponse> createInvite(@AuthenticationPrincipal final UUID id) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inviteService.createInvite(id));
    }
}
