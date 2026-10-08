package com.packrat.backend.exception;

import java.util.UUID;

public class InviteNotFoundException extends RuntimeException {
    public InviteNotFoundException(UUID id) { super("Invite " + id + " not found or expired"); }
}
