package com.packrat.backend.service;

import com.packrat.backend.dto.InviteResponse;
import com.packrat.backend.entity.Invite;

public class InviteService {

    public InviteService() {

    }

    
    
    public InviteResponse toResponse(final Invite invite) {
        return new InviteResponse();
    }
}
