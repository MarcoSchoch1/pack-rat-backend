package com.packrat.backend.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import com.packrat.backend.dto.InviteResponse;
import com.packrat.backend.entity.Invite;
import com.packrat.backend.entity.User;
import com.packrat.backend.exception.InviteNotFoundException;
import com.packrat.backend.exception.ItemNotFoundException;
import com.packrat.backend.exception.UserNotFoundException;
import com.packrat.backend.repository.InviteRepository;
import com.packrat.backend.repository.UserRepository;

@Service 
public class InviteService {

    private final String frontendUrl;

    private final InviteRepository inviteRepository;
    private final UserRepository userRepository;

    public InviteService(final InviteRepository inviteRepository, final UserRepository userRepository, @Value("${app.frontend-url}") final String frontendUrl) {
        this.inviteRepository = inviteRepository;
        this.userRepository = userRepository;
        this.frontendUrl = frontendUrl;
    }

    public void checkInvite(final UUID token) {
        final Invite invite = inviteRepository.findById(token).orElseThrow(() -> new InviteNotFoundException(token));
        if (invite.getValidUntil().isBefore(LocalDateTime.now()) || (invite.getUsedAt() != null && invite.getUsedBy() != null)) {
            throw new InviteNotFoundException(token);
        }
    }

    public InviteResponse createInvite(final UUID userId) {
        final User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        final Invite invite = new Invite();
        invite.setCreatedBy(user);
        invite.setCreatedAt(LocalDateTime.now());
        invite.setValidUntil(LocalDateTime.now().plusDays(7));
        inviteRepository.save(invite);
        return toResponse(createUrl(invite), invite.getValidUntil());
    }

    private String createUrl(final Invite invite) {
        return UriComponentsBuilder.fromUriString(frontendUrl).path("/register").queryParam("invite", invite.getId()).toUriString();
    }
    
    public InviteResponse toResponse(final String url, final LocalDateTime validUntil) {
        return new InviteResponse(url, validUntil);
    }
}
