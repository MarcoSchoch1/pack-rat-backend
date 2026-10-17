package com.packrat.backend.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.packrat.backend.dto.InviteResponse;
import com.packrat.backend.service.InviteService;

@RestController 
@RequestMapping("api/invites")
public class InviteController {

    private InviteService inviteService;
    
    public InviteController(final InviteService inviteService) {
        this.inviteService = inviteService;
    }

    @GetMapping("/{inviteToken}")
    public ResponseEntity<Void> checkInvite(@PathVariable final UUID inviteToken) {
        inviteService.checkInvite(inviteToken);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<InviteResponse> createInvite(@AuthenticationPrincipal final UUID id) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inviteService.createInvite(id));
    }
}
