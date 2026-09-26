package com.packrat.backend.exception;

import java.util.UUID;

public class CollectionNotFoundException extends RuntimeException {
    public CollectionNotFoundException(UUID id) { super("Collection " + id + " not found"); }
}
